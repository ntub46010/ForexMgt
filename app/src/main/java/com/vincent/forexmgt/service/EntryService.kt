package com.vincent.forexmgt.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.google.firebase.firestore.*
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Entry

class EntryService : Service() {

    private lateinit var db: FirebaseFirestore
    private lateinit var collection: CollectionReference

    private lateinit var bookService: BookService

    override fun onBind(intent: Intent?): IBinder {
        bindService(Intent(this, BookService::class.java), bookServiceConn, Context.BIND_AUTO_CREATE)
        return CollectionBinder()
    }

    fun createEntry(entry: Entry) {
        collection
            .add(entry)
            .addOnSuccessListener { documentRef ->
                entry.id = documentRef.id
                createEntryPostProcess(entry)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EntryService, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createEntryPostProcess(entry: Entry) {
        db.runTransaction { transaction ->
            updateEntryInfo(transaction, entry)
            updateBookAsset(transaction, entry)
            null
        }
            .addOnSuccessListener {
                Toast.makeText(this@EntryService, getString(R.string.create_successfully), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EntryService, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBookAsset(transaction: Transaction, entry: Entry) {
        var deltaFcyTotalAmt = entry.fcyAmt
        var deltaTwdTotalCost = entry.twdAmt
        if (entry.type == EntryType.DEBIT) {
            deltaFcyTotalAmt = -entry.fcyAmt
            deltaTwdTotalCost = -entry.twdAmt
        }

        val bookDoc = bookService.getBookDoc(entry.bookId)
        val bookSnapshot = transaction.get(bookDoc)
        val newFcyTotalAmt = bookSnapshot.getDouble(Constants.PROPERTY_FCY_TOTAL_AMT)!! + deltaFcyTotalAmt
        val newTwdTotalCost = bookSnapshot.getLong(Constants.PROPERTY_TWD_TOTAL_COST)!! + deltaTwdTotalCost

        val patchData = mapOf(
            Constants.PROPERTY_FCY_TOTAL_AMT to newFcyTotalAmt,
            Constants.PROPERTY_TWD_TOTAL_COST to newTwdTotalCost
        )

        transaction.set(bookDoc, patchData, SetOptions.merge())
    }

    private fun updateEntryInfo(transaction: Transaction, entry: Entry) {
        val entryDoc = getEntryDoc(entry.id)
        if (entry.type == EntryType.CREDIT) {
            transaction.update(entryDoc, Constants.PROPERTY_ID, entry.id)
            return
        }

        val bookDoc = bookService.getBookDoc(entry.bookId)
        val bookSnapshot = transaction.get(bookDoc)

        val fcyTotalAmt = bookSnapshot.getLong(Constants.PROPERTY_FCY_TOTAL_AMT)!!
        val twdTotalCost = bookSnapshot.getLong(Constants.PROPERTY_TWD_TOTAL_COST)!!
        val twdBV = Math.round(twdTotalCost * entry.fcyAmt / fcyTotalAmt).toInt()

        val patchData = mapOf(
            Constants.PROPERTY_ID to entry.id,
            Constants.PROPERTY_TWD_BV to twdBV
        )

        transaction.set(entryDoc, patchData, SetOptions.merge())
    }

    fun getEntryDoc(id: String) = collection.document(id)

    private val bookServiceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bookService = (service as BookService.CollectionBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    internal inner class CollectionBinder : Binder() {
        fun getService(): EntryService {
            val service = this@EntryService
            service.db = FirebaseFirestore.getInstance()
            service.collection = db.collection(Constants.COLLECTION_ENTRY)
            return service
        }
    }
}