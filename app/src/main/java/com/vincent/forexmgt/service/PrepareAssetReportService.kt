package com.vincent.forexmgt.service

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.widget.Toast
import com.vincent.forexmgt.*
import com.vincent.forexmgt.entity.*
import java.io.Serializable

class PrepareAssetReportService : IntentService("PrepareAssetReportService") {

    private var bookService = ForExMgtApp.bookService!!
    private var entryService = ForExMgtApp.entryService!!

    override fun onHandleIntent(intent: Intent?) {
        val receiver = intent?.getParcelableExtra(Constants.KEY_RECEIVER) as ResultReceiver
        val rateList = intent.getSerializableExtra(Constants.KEY_RATE) as List<ExchangeRate>

        val returnOp = object : Operator {
            override fun execute(result: Any?) {
                val assetReport = result as AssetReport
                val bundle = Bundle()
                bundle.putSerializable(Constants.KEY_REPORT, assetReport as Serializable)
                receiver.send(0, bundle)
            }
        }

        val rateMap = convertRateMap(rateList)
        loadBooks(rateMap, returnOp)
    }

    private fun loadBooks(rateMap: Map<CurrencyType, Double>, returnOp: Operator) {
        val operator = object : Operator {
            override fun execute(result: Any?) {
                val books = sortBooksByCurrencyType(result as List<Book>)
                loadEntries(books, rateMap, returnOp)
            }
        }

        bookService.loadBooks(operator)
    }

    private fun loadEntries(books: List<Book>, rateMap: Map<CurrencyType, Double>, returnOp: Operator) {
        val bookIds = mutableSetOf<String>()
        for (book in books) {
            bookIds.add(book.obtainId())
        }

        val operator = object : Operator {
            override fun execute(result: Any?) {
                if (result is Exception) {
                    Toast.makeText(this@PrepareAssetReportService, "${getString(R.string.load_entry_error)}\n${result.message}", Toast.LENGTH_SHORT).show()
                    return
                }

                val entries = result as List<Entry>
                generateBookSummaries(books, entries, rateMap, returnOp)
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

    private fun generateBookSummaries(currencySortedBooks: List<Book>, entries: List<Entry>, rateMap: Map<CurrencyType, Double>, returnOp: Operator) {
        val bookMap = mutableMapOf<String, Book>()
        val entriesMap = linkedMapOf<String, MutableList<Entry>>()

        for (book in currencySortedBooks) {
            bookMap[book.obtainId()] = book
            entriesMap[book.obtainId()] = mutableListOf()
        }

        for (entry in entries) {
            entriesMap[entry.bookId]?.add(entry)
        }

        val summariesGroup = mutableListOf<MutableList<SubAssetSummary>>()

        for (entryList in entriesMap.values) {
            val summaries = mutableListOf<SubAssetSummary>()

            if (entryList.isNotEmpty()) {
                val bookId = entryList[0].bookId
                val bookName = bookMap[bookId]?.name
                summaries.add(generateBookSummary(bookName!!, entriesMap[bookId]!!, rateMap))
            }

            summariesGroup.add(summaries)
        }

        generateAssetReport(currencySortedBooks, summariesGroup, returnOp)
    }

    private fun generateBookSummary(bookName: String, entries: List<Entry>, rateMap: Map<CurrencyType, Double>): SubAssetSummary {
        var fcyAmt = 0.0
        var twdCost = 0

        for (entry in entries) {
            if (entry.type == EntryType.CREDIT) {
                fcyAmt += entry.fcyAmt
                twdCost += entry.twdCost!!
            } else if (entry.type == EntryType.DEBIT) {
                twdCost -= Math.round(twdCost * (entry.fcyAmt / fcyAmt)).toInt()
                fcyAmt -= entry.fcyAmt
            }
        }

        val currencyType = CurrencyType.fromCode(entries[0].fcyType)!!
        val twdPV = Math.round(fcyAmt * rateMap[currencyType]!!).toInt()

        return SubAssetSummary(bookName, fcyAmt, twdPV, twdCost)
    }

    private fun generateAssetReport(currencySortedBooks: List<Book>, summariesGroup: List<List<SubAssetSummary>>, returnOp: Operator) {
        val genAssetSummaries = mutableListOf<GeneralAssetSummary>()

        for (i in 0 until currencySortedBooks.size) {
            val summaries = summariesGroup[i]
            var fcyAmt = 0.0
            var twdPV = 0
            var twdCost = 0

            for (summary in summaries) {
                fcyAmt += summary.fcyAmt
                twdPV += summary.twdPV
                twdCost += summary.twdCost
            }

            val genSummary = GeneralAssetSummary(
                currencySortedBooks[i].currencyType,
                fcyAmt,
                twdPV,
                Math.round(twdCost * 10000 / fcyAmt) / 10000.0
            )

            genAssetSummaries.add(genSummary)
        }

        val assetReport = AssetReport(genAssetSummaries, summariesGroup)
        returnOp.execute(assetReport)
    }

}