package com.vincent.forexmgt.service

import android.app.Service
import android.content.Intent
import android.os.*
import com.google.firebase.firestore.*
import com.vincent.forexmgt.*
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.entity.Entry
import com.vincent.forexmgt.entity.ExchangeRate
import com.vincent.forexmgt.util.DocumentConverter
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.util.*

class EntryService : Service() {

    private lateinit var db: FirebaseFirestore
    private lateinit var collection: CollectionReference

    private lateinit var bookService: BookService

    override fun onBind(intent: Intent?): IBinder {
        return CollectionBinder()
    }

    fun createEntry(entry: Entry, callback: Callback<Entry>) {
        when (entry.type) {
            EntryType.CREDIT -> insertEntry(entry, callback)
            EntryType.DEBIT -> createDebitEntry(entry, callback)
            EntryType.BALANCE -> insertEntry(entry, callback)
        }
    }

    fun previewBalanceEntry(book: Book, callback: Callback<Entry>) {
        val receiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                val data = resultData?.getSerializable(Constants.KEY_DATA)
                if (data is Exception) {
                    callback.onError(data)
                    return
                }

                val rates = data as List<ExchangeRate>
                createBalanceEntry(book, rates, callback)
            }
        }

        val intent = Intent(this, LoadingExchangeRateService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        intent.putExtra(Constants.KEY_BANK_NAME, book.bank?.name)
        startService(intent)
    }

    fun loadEntries(bookIds: Set<String>, callback: Callback<List<Entry>>) {
        for (id in bookIds) {
            collection.whereEqualTo(Constants.PROPERTY_BOOK_ID, id)
        }

        collection
            .get()
            .addOnSuccessListener {
                val entries = DocumentConverter.toEntries(it)
                callback.onExecute(entries)
            }
            .addOnFailureListener {
                callback.onError(it)
            }
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

    private fun loadEntries(bookId: String, callback: Callback<List<Entry>>) {
        collection
            .whereEqualTo(Constants.PROPERTY_BOOK_ID, bookId)
            .get()
            .addOnSuccessListener {
                val entries = DocumentConverter.toEntries(it)
                callback.onExecute(entries)
            }
            .addOnFailureListener {
                callback.onError(it)
            }
    }

    private fun createDebitEntry(entry: Entry, callback: Callback<Entry>) {
        val loadEntriesCb = object : Callback<List<Entry>> {
            override fun onExecute(data: List<Entry>) {
                var fcyTotalAmtDec = BigDecimal(0.0)
                var twdTotalCost = 0
                var dec: BigDecimal

                for (en in data) {
                    dec = BigDecimal(en.fcyAmt)

                    if (en.type == EntryType.CREDIT) {
                        fcyTotalAmtDec = fcyTotalAmtDec.add(dec)
                        twdTotalCost += en.twdCost
                    } else if (en.type == EntryType.DEBIT) {
                        fcyTotalAmtDec = fcyTotalAmtDec.subtract(dec)
                        twdTotalCost -= en.twdCost
                    }
                }

                val fcyTotalAmt = fcyTotalAmtDec.toDouble()
                if (fcyTotalAmt < entry.fcyAmt) {
                    callback.onError(Exception(getString(R.string.insufficient_fcy_amt)))
                    return
                }

                generateDebitEntry(entry, fcyTotalAmt, twdTotalCost, callback)
            }

            override fun onError(e: Exception) {
                callback.onError(e)
            }
        }

        loadEntries(entry.bookId, loadEntriesCb)
    }

    private fun createBalanceEntry(book: Book, exchangeRates: List<ExchangeRate>, callback: Callback<Entry>) {
        val loadEntriesCb = object : Callback<List<Entry>> {
            override fun onExecute(data: List<Entry>) {
                var fcyTotalAmtDec = BigDecimal(0.0)
                var twdTotalCost = 0
                var dec: BigDecimal

                for (en in data) {
                    dec = BigDecimal(en.fcyAmt)

                    if (en.type == EntryType.CREDIT) {
                        fcyTotalAmtDec = fcyTotalAmtDec.add(dec)
                        twdTotalCost += en.twdCost
                    } else if (en.type == EntryType.DEBIT) {
                        fcyTotalAmtDec = fcyTotalAmtDec.subtract(dec)
                        twdTotalCost -= en.twdCost
                    }
                }

                val fcyTotalAmt = fcyTotalAmtDec.toDouble()
                val spotRate = exchangeRates.firstOrNull {
                    StringUtils.equals(it.currencyType?.name, book.currencyType?.name)
                }!!

                generateBalanceEntry(book, spotRate, fcyTotalAmt, twdTotalCost, callback)
            }

            override fun onError(e: Exception) {
                callback.onError(e)
            }
        }

        loadEntries(book.obtainId(), loadEntriesCb)
    }

    private fun generateDebitEntry(entry: Entry, fcyTotalAmt: Double, twdTotalCost: Int, callback: Callback<Entry>) {
        entry.twdCost = Math.round(twdTotalCost * entry.fcyAmt / fcyTotalAmt).toInt()
        insertEntry(entry, callback)
    }

    private fun generateBalanceEntry(book: Book, spotRate: ExchangeRate, fcyTotalAmt: Double, twdTotalCost: Int, callback: Callback<Entry>) {
        val presentValue = Math.round(fcyTotalAmt * spotRate.debit).toInt()

        val entry = Entry(
            book.obtainId(),
            Date(),
            EntryType.BALANCE,
            book.currencyType,
            fcyTotalAmt,
            presentValue,
            spotRate.debit,
            twdTotalCost
        )

        callback.onExecute(entry)
    }

    private fun insertEntry(entry: Entry, callback: Callback<Entry>) {
        collection
            .add(entry)
            .addOnSuccessListener {
                entry.defineId(it.id)
                callback.onExecute(entry)
            }
            .addOnFailureListener {
                callback.onError(it)
            }
    }

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