package com.vincent.forexmgt.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.firebase.firestore.*
import com.vincent.forexmgt.*
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.entity.Entry
import com.vincent.forexmgt.entity.ExchangeRate
import com.vincent.forexmgt.entity.SubAssetSummary
import com.vincent.forexmgt.util.DocumentConverter
import org.apache.commons.lang3.StringUtils
import java.util.*

class EntryService : Service() {

    private lateinit var db: FirebaseFirestore
    private lateinit var collection: CollectionReference

    private lateinit var bookService: BookService

    override fun onBind(intent: Intent?): IBinder {
        return CollectionBinder()
    }

    fun createEntry(entry: Entry, operator: Operator) {
        collection
            .add(entry)
            .addOnSuccessListener { documentRef ->
                entry.defineId(documentRef.id)

                if (entry.type == EntryType.BALANCE) {
                    operator.execute(null)
                } else {
                    createEntryPostProcess(entry, operator)
                }
            }
            .addOnFailureListener { e ->
                operator.execute(e)
            }
    }

    fun generateBalanceEntry(book: Book, exchangeRates: List<ExchangeRate>): Entry {
        val spotRate = exchangeRates.find { rate ->
            StringUtils.equals(rate.currencyType?.name, book.currencyType?.name)
        }!!

        val presentValue = Math.round(book.fcyTotalAmt * spotRate.debit).toInt()
        val profit = presentValue - book.twdTotalCost

        val entry = Entry(
            book.obtainId(),
            Date(),
            EntryType.BALANCE,
            book.currencyType!!.name,
            book.fcyTotalAmt,
            presentValue,
            spotRate.debit
        )

        entry.twdProfit = profit

        return entry
    }

    fun loadEntries(bookIds: Set<String>, operator: Operator) {
        for (id in bookIds) {
            collection.whereEqualTo(Constants.PROPERTY_BOOK_ID, id)
        }

        collection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val entries = DocumentConverter.toEntries(querySnapshot)
                operator.execute(entries)
            }
            .addOnFailureListener { e ->
                operator.execute(e)
            }
    }

    private fun createEntryPostProcess(entry: Entry, operator: Operator) {
        db.runTransaction { transaction ->
            val bookDoc = bookService.getBookDoc(entry.bookId)
            val bookSnapshot = transaction.get(bookDoc)

            if (entry.type == EntryType.DEBIT) {
                updateEntryInfo(transaction, bookSnapshot, entry)
            }

            if (entry.type != EntryType.BALANCE) {
                updateBookAsset(transaction, bookSnapshot, entry)
            }
            null
        }
            .addOnSuccessListener {
                operator.execute(null)
            }
            .addOnFailureListener { e ->
                operator.execute(e)
            }
    }

    private fun updateEntryInfo(transaction: Transaction, bookSnapshot: DocumentSnapshot, entry: Entry) {
        val fcyTotalAmt = bookSnapshot.getLong(Constants.PROPERTY_FCY_TOTAL_AMT)!!
        val twdTotalCost = bookSnapshot.getLong(Constants.PROPERTY_TWD_TOTAL_COST)!!
        val twdBV = Math.round(twdTotalCost * entry.fcyAmt / fcyTotalAmt).toInt()
        val profit = entry.twdAmt - twdBV

        val entryDoc = getEntryDoc(entry.obtainId())
        transaction.update(entryDoc, Constants.PROPERTY_TWD_PROFIT, profit)
    }

    private fun updateBookAsset(transaction: Transaction, bookSnapshot: DocumentSnapshot, entry: Entry) {
        val oldFcyTotalAmt = bookSnapshot.getDouble(Constants.PROPERTY_FCY_TOTAL_AMT)!!
        val oldTwdTotalCost = bookSnapshot.getLong(Constants.PROPERTY_TWD_TOTAL_COST)!!

        val deltaFcyTotalAmt = if (entry.type == EntryType.CREDIT) entry.fcyAmt
            else -entry.fcyAmt
        val deltaTwdTotalCost = if (entry.type == EntryType.CREDIT) entry.twdCost!!
            else -Math.round(oldTwdTotalCost * (entry.fcyAmt / oldFcyTotalAmt)).toInt()

        val newFcyTotalAmt = oldFcyTotalAmt + deltaFcyTotalAmt
        val newTwdTotalCost = oldTwdTotalCost + deltaTwdTotalCost

        val patchData = mapOf(
            Constants.PROPERTY_FCY_TOTAL_AMT to newFcyTotalAmt,
            Constants.PROPERTY_TWD_TOTAL_COST to newTwdTotalCost)

        val bookDoc = bookService.getBookDoc(entry.bookId)
        transaction.set(bookDoc, patchData, SetOptions.merge())
    }

    fun subscribeEntries(bookId: String, entryType: EntryType, operator: Operator): ListenerRegistration {
        return collection
            .whereEqualTo(Constants.PROPERTY_BOOK_ID, bookId)
            .whereEqualTo(Constants.PROPERTY_TYPE, entryType.name)
            .orderBy(Constants.PROPERTY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    operator.execute(e)
                } else {
                    val entries = DocumentConverter.toEntries(querySnapshot)
                    operator.execute(entries)
                }
            }
    }

    fun generateBookSummaries(currencySortedBooks: List<Book>, entries: List<Entry>, rateMap: Map<CurrencyType, Double>, displayOp: Operator) {
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

        bookService.generateAssetReport(currencySortedBooks, summariesGroup, displayOp)
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

    fun getEntryDoc(id: String) = collection.document(id)

    internal inner class CollectionBinder : Binder() {
        fun getService(): EntryService {
            val service = this@EntryService
            service.db = FirebaseFirestore.getInstance()
            service.collection = db.collection(Constants.COLLECTION_ENTRY)
            service.bookService = ForExMgtApp.bookService!!
            return service
        }
    }
}