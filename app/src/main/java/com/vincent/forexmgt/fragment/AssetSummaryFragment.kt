package com.vincent.forexmgt.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.*
import com.vincent.forexmgt.adapter.AssetSummaryAdapter
import com.vincent.forexmgt.entity.*
import com.vincent.forexmgt.service.LoadingExchangeRateService

class AssetSummaryFragment : Fragment() {

    @BindView(R.id.listView) lateinit var listView: ExpandableListView

    private var bookService = ForExMgtApp.bookService!!
    private var entryService = ForExMgtApp.entryService!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_asset_summary, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        displayMockData()

        val receiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultData == null) {
                    Toast.makeText(context, getString(R.string.load_exchange_rate_error), Toast.LENGTH_SHORT).show()
                    return
                }

                val displayOp = object : Operator {
                    override fun execute(result: Any?) {
                        val assetReport = result as AssetReport
                        listView.setAdapter(AssetSummaryAdapter(context!!,
                            assetReport.generalAssetSummaries, assetReport.subAssetSummariesGroup))
                    }
                }

                val rateMap = convertRateMap(resultData.getSerializable(Constants.KEY_RATE) as List<ExchangeRate>)
                loadBooks(rateMap, displayOp)
            }
        }

        val intent = Intent(context, LoadingExchangeRateService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        context?.startService(intent)
    }

    private fun loadBooks(rateMap: Map<CurrencyType, Double>, displayOp: Operator) {
        val operator = object : Operator {
            override fun execute(result: Any?) {
                val books = sortBooksByCurrencyType(result as List<Book>)
                loadEntries(books, rateMap, displayOp)
            }
        }

        bookService.loadBooks(operator)
    }

    private fun loadEntries(books: List<Book>, rateMap: Map<CurrencyType, Double>, displayOp: Operator) {
        val bookIds = mutableSetOf<String>()
        for (book in books) {
            bookIds.add(book.obtainId())
        }

        val operator = object : Operator {
            override fun execute(result: Any?) {
                if (result is Exception) {
                    Toast.makeText(context, "${getString(R.string.load_entry_error)}\n${result.message}", Toast.LENGTH_SHORT).show()
                    return
                }

                val entries = result as List<Entry>
                entryService.generateBookSummaries(books, entries, rateMap, displayOp)
            }
        }

        entryService.loadEntries(bookIds, operator)
    }

    private fun convertRateMap(rateList: List<ExchangeRate>): Map<CurrencyType, Double> {
        val rateMap = mutableMapOf<CurrencyType, Double>()

        for (rate in rateList) {
            rateMap[rate.currencyType!!] = rate.debit
        }

        return rateMap
    }

    private fun sortBooksByCurrencyType(books: List<Book>): List<Book> {
        val sortedBooks = mutableListOf<Book>()
        val bookMap = linkedMapOf<String, MutableList<Book>>()

        for (type in CurrencyType.values()) {
            bookMap[type.name] = mutableListOf()
        }

        for (book in books) {
            bookMap[book.currencyType!!.name]?.add(book)
        }

        for (bookList in bookMap.values) {
            sortedBooks.addAll(bookList)
        }

        return sortedBooks
    }

    private fun displayMockData() {
        val sub1 = SubAssetSummary(
            "美金帳簿1",
            1000.0,
            31000,
            30000
        )

        val sub2 = SubAssetSummary(
            "美金帳簿2",
            2000.0,
            62000,
            60000
        )

        val gen1 = GeneralAssetSummary(
            CurrencyType.USD,
            3000.0,
            93000,
            30.0
        )

        val sub3 = SubAssetSummary(
            "歐元帳簿1",
            100.0,
            3600,
            3500
        )

        val sub4 = SubAssetSummary(
            "歐元帳簿2",
            300.0,
            10800,
            10500
        )

        val gen2 = GeneralAssetSummary(
            CurrencyType.EUR,
            400.0,
            14400,
            35.0
        )

        val gens = listOf(gen1, gen2)

        val subs = listOf(
            listOf(sub1, sub2),
            listOf(sub3, sub4)
        )

        listView.setAdapter(AssetSummaryAdapter(context!!, gens, subs))
    }
}