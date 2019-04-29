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

    fun createEntry(entry: Entry, callback: Callback<Entry>) {
        collection
            .add(entry)
            .addOnSuccessListener { documentRef ->
                entry.defineId(documentRef.id)

                if (entry.type == EntryType.BALANCE) {
                    callback.onExecute(entry)
                } else {
                    createEntryPostProcess(entry, callback)
                }
            }
            .addOnFailureListener { e ->
                callback.onError(e)
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

    fun loadEntries(bookIds: Set<String>, callback: Callback<List<Entry>>) {
        for (id in bookIds) {
            collection.whereEqualTo(Constants.PROPERTY_BOOK_ID, id)
        }

        collection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val entries = DocumentConverter.toEntries(querySnapshot)
                callback.onExecute(entries)
            }
            .addOnFailureListener { e ->
                callback.onError(e)
            }
    }

    private fun createEntryPostProcess(entry: Entry, callback: Callback<Entry>) {
        db.runTransaction { transaction ->
            val bookDoc = bookService.getBookDoc(entry.bookId)
            val bookSnapshot = transaction.get(bookDoc)

            if (entry.type == EntryType.DEBIT) {
                updateEntryInfo(transaction, bookSnapshot, entry)
            }

            if (entry.type != EntryType.BALANCE) {
                updateBookAsset(transaction, bookSnapshot, entry)
            }

            entry
        }
            .addOnSuccessListener { data ->
                callback.onExecute(data)
            }
            .addOnFailureListener { e ->
                callback.onError(e)
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

    fun subscribeEntries(bookId: String, entryType: EntryType, callback: Callback<List<Entry>>): ListenerRegistration {
        return collection
            .whereEqualTo(Constants.PROPERTY_BOOK_ID, bookId)
            .whereEqualTo(Constants.PROPERTY_TYPE, entryType.name)
            .orderBy(Constants.PROPERTY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, e ->
                if (e == null) {
                    val entries = DocumentConverter.toEntries(querySnapshot)
                    callback.onExecute(entries)
                } else {
                    callback.onError(e)
                }
            }
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