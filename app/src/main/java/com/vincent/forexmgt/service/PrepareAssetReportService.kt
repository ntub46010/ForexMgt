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

    private fun loadEntries(allBooks: List<Book>, rateMap: Map<CurrencyType, Double>, returnOp: Operator) {
        val bookIds = mutableSetOf<String>()
        for (book in allBooks) {
            bookIds.add(book.obtainId())
        }

        val operator = object : Operator {
            override fun execute(result: Any?) {
                if (result is Exception) {
                    Toast.makeText(this@PrepareAssetReportService, "${getString(R.string.load_entry_error)}\n${result.message}", Toast.LENGTH_SHORT).show()
                    return
                }

                val allEntries = result as List<Entry>
                generateBookReports(allBooks, allEntries, rateMap, returnOp)
            }
        }

        entryService.loadEntries(bookIds, operator)
    }

    private fun generateBookReports(allBooks: List<Book>, allEntries: List<Entry>, rateMap: Map<CurrencyType, Double>, returnOp: Operator) {
        val bookMap = mutableMapOf<String, Book>()
        val bookToEntriesMap = linkedMapOf<String, MutableList<Entry>>()
        var currencyToBookReportsMap = mutableMapOf<CurrencyType, MutableList<BookAssetReport>>()

        // init map
        for (book in allBooks) {
            bookMap[book.obtainId()] = book
            bookToEntriesMap[book.obtainId()] = mutableListOf()
            currencyToBookReportsMap[book.currencyType!!] = mutableListOf()
        }

        // classify all entries by book they belong to
        for (entry in allEntries) {
            bookToEntriesMap[entry.bookId]?.add(entry)
        }

        // generate reports of every book
        for (entriesInBook in bookToEntriesMap.values) {

            // generate report of that book
            if (entriesInBook.isNotEmpty()) {
                val bookId = entriesInBook[0].bookId
                val book = bookMap[bookId]!!
                val bookName = book.name

                val bookReport = generateBookReport(bookName, entriesInBook, rateMap)
                currencyToBookReportsMap[book.currencyType]?.add(bookReport)
            }
        }

        currencyToBookReportsMap = currencyToBookReportsMap.filter { pair ->
            !pair.value.isNullOrEmpty()
        }.toMutableMap()

        generateAssetReport(currencyToBookReportsMap, returnOp)
    }

    private fun generateAssetReport(currencyToBookReportsMap: Map<CurrencyType, List<BookAssetReport>>, returnOp: Operator) {
        val currencyReports = mutableListOf<CurrencyAssetReport>()

        for (currencyType in CurrencyType.values()) {
            val bookReports = currencyToBookReportsMap[currencyType] ?: continue

            var fcyAmt = 0.0
            var twdPV = 0
            var twdCost = 0

            for (bookReport in bookReports) {
                fcyAmt += bookReport.fcyAmt
                twdPV += bookReport.twdPV
                twdCost += bookReport.twdCost
            }

            val currencyReport = CurrencyAssetReport(
                currencyType,
                fcyAmt,
                twdPV,
                0.0
            )

            currencyReport.avgCost =
                if (fcyAmt == 0.0) 0.0
                else Math.round(twdCost * 10000 / fcyAmt) / 10000.0

            currencyReports.add(currencyReport)
        }

        val assetReport = AssetReport(currencyReports, currencyToBookReportsMap)
        returnOp.execute(assetReport)
    }

    private fun generateBookReport(bookName: String, entriesInBook: List<Entry>, rateMap: Map<CurrencyType, Double>): BookAssetReport {
        var fcyAmt = 0.0
        var twdCost = 0

        for (entry in entriesInBook) {
            if (entry.type == EntryType.CREDIT) {
                twdCost += entry.twdCost!!
                fcyAmt += entry.fcyAmt
            } else if (entry.type == EntryType.DEBIT) {
                twdCost -= Math.round(twdCost * (entry.fcyAmt / fcyAmt)).toInt()
                fcyAmt -= entry.fcyAmt
            }
        }

        val currencyType = CurrencyType.fromCode(entriesInBook[0].fcyType)!!
        val twdPV = Math.round(fcyAmt * rateMap[currencyType]!!).toInt()

        return BookAssetReport(entriesInBook[0].bookId, bookName, fcyAmt, twdPV, twdCost)
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

}