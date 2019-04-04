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
import com.vincent.forexmgt.Operator
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

    fun createEntry(entry: Entry, operator: Operator) {
        collection
            .add(entry)
            .addOnSuccessListener { documentRef ->
                entry.id = documentRef.id
                createEntryPostProcess(entry, operator)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EntryService, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_LONG).show()
                operator.execute(null)
            }
    }

    private fun createEntryPostProcess(entry: Entry, operator: Operator) {
        db.runTransaction { transaction ->
            val bookDoc = bookService.getBookDoc(entry.bookId)
            val bookSnapshot = transaction.get(bookDoc)
            updateEntryInfo(transaction, bookSnapshot, entry)
            updateBookAsset(transaction, bookSnapshot, bookDoc, entry)
            null
        }
            .addOnSuccessListener {
                Toast.makeText(this@EntryService, getString(R.string.create_successfully), Toast.LENGTH_SHORT).show()
                operator.execute(null)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EntryService, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_LONG).show()
                operator.execute(null)
            }
    }

    private fun updateEntryInfo(transaction: Transaction, bookSnapshot: DocumentSnapshot, entry: Entry) {
        val entryDoc = getEntryDoc(entry.id)
        if (entry.type == EntryType.CREDIT) {
            transaction.update(entryDoc, Constants.PROPERTY_ID, entry.id)
            return
        }

        val fcyTotalAmt = bookSnapshot.getLong(Constants.PROPERTY_FCY_TOTAL_AMT)!!
        val twdTotalCost = bookSnapshot.getLong(Constants.PROPERTY_TWD_TOTAL_COST)!!
        val twdBV = Math.round(twdTotalCost * entry.fcyAmt / fcyTotalAmt).toInt()

        val patchData = mapOf(
            Constants.PROPERTY_ID to entry.id,
            Constants.PROPERTY_TWD_BV to twdBV)

        transaction.set(entryDoc, patchData, SetOptions.merge())
    }

    private fun updateBookAsset(transaction: Transaction, bookSnapshot: DocumentSnapshot,
                                bookDoc: DocumentReference, entry: Entry) {
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

        transaction.set(bookDoc, patchData, SetOptions.merge())
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