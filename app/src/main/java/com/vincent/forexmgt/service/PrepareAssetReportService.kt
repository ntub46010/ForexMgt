package com.vincent.forexmgt.service

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.vincent.forexmgt.*
import com.vincent.forexmgt.entity.*
import java.io.Serializable

class PrepareAssetReportService : IntentService("PrepareAssetReportService") {

    private var bookService = ForExMgtApp.bookService!!
    private var entryService = ForExMgtApp.entryService!!

    override fun onHandleIntent(intent: Intent?) {
        val receiver = intent?.getParcelableExtra(Constants.KEY_RECEIVER) as ResultReceiver
        val rateList = intent.getSerializableExtra(Constants.KEY_RATE) as List<ExchangeRate>

        val returnCb = object : Callback<AssetReport> {
            override fun onExecute(data: AssetReport) {
                val bundle = Bundle()
                bundle.putSerializable(Constants.KEY_DATA, data as Serializable)
                receiver.send(0, bundle)
            }

            override fun onError(e: Exception) {
                val bundle = Bundle()
                bundle.putSerializable(Constants.KEY_DATA, e as Serializable)
                receiver.send(0, bundle)
            }
        }

        val rateMap = convertRateMap(rateList)
        loadBooks(rateMap, returnCb)
    }

    private fun loadBooks(rateMap: Map<CurrencyType, Double>, returnCb: Callback<AssetReport>) {
        val callback = object : Callback<List<Book>> {
            override fun onExecute(data: List<Book>) {
                val books = data.sortedBy { it.currencyType?.ordinal }
                loadEntries(books, rateMap, returnCb)
            }

            override fun onError(e: Exception) {
                returnCb.onError(e)
            }
        }

        bookService.loadBooks(callback)
    }

    private fun loadEntries(allBooks: List<Book>, rateMap: Map<CurrencyType, Double>, returnCb: Callback<AssetReport>) {
        val bookIds = mutableSetOf<String>()
        for (book in allBooks) {
            bookIds.add(book.obtainId())
        }

        val callback = object : Callback<List<Entry>> {
            override fun onExecute(data: List<Entry>) {
                generateBookReports(allBooks, data, rateMap, returnCb)
            }

            override fun onError(e: Exception) {
                returnCb.onError(e)
            }
        }

        entryService.loadEntries(bookIds, callback)
    }

    private fun generateBookReports(allBooks: List<Book>, allEntries: List<Entry>, rateMap: Map<CurrencyType, Double>, returnCb: Callback<AssetReport>) {
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

        currencyToBookReportsMap = currencyToBookReportsMap
            .filter { !it.value.isNullOrEmpty() }
            .toMutableMap()

        generateAssetReport(currencyToBookReportsMap, returnCb)
    }

    private fun generateAssetReport(currencyToBookReportsMap: Map<CurrencyType, List<BookAssetReport>>, returnCb: Callback<AssetReport>) {
        val currencyReports = mutableListOf<CurrencyAssetReport>()

        for (pair in currencyToBookReportsMap.entries) {
            var fcyAmt = 0.0
            var twdPV = 0
            var twdCost = 0

            for (bookReport in pair.value) {
                fcyAmt += bookReport.fcyAmt
                twdPV += bookReport.twdPV
                twdCost += bookReport.twdCost
            }

            val currencyReport = CurrencyAssetReport(
                pair.key,
                fcyAmt,
                twdPV,
                0.0
            )

            currencyReport.avgCost =
                if (fcyAmt == 0.0) 0.0
                else Math.round(twdCost * 10000 / fcyAmt) / 10000.0

            currencyReports.add(currencyReport)
        }

        currencyReports.sortBy { it.currencyType?.ordinal }

        val assetReport = AssetReport(currencyReports, currencyToBookReportsMap)
        returnCb.onExecute(assetReport)
    }

    private fun generateBookReport(bookName: String, entriesInBook: List<Entry>, rateMap: Map<CurrencyType, Double>): BookAssetReport {
        var fcyAmt = 0.0
        var twdCost = 0

        for (entry in entriesInBook) {
            if (entry.type == EntryType.CREDIT) {
                twdCost += entry.twdCost
                fcyAmt += entry.fcyAmt
            } else if (entry.type == EntryType.DEBIT) {
                twdCost -= entry.twdCost
                fcyAmt -= entry.fcyAmt
            }
        }

        val currencyType = entriesInBook[0].currencyType
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

}