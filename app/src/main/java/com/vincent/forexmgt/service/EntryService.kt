package com.vincent.forexmgt.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.firebase.firestore.*
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.ForExMgtApp
import com.vincent.forexmgt.Operator
import com.vincent.forexmgt.entity.Entry
import com.vincent.forexmgt.util.DocumentConverter

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
                entry.id = documentRef.id
                createEntryPostProcess(entry, operator)
            }
            .addOnFailureListener { e ->
                operator.execute(e)
            }
    }

    private fun createEntryPostProcess(entry: Entry, operator: Operator) {
        db.runTransaction { transaction ->
            val bookDoc = bookService.getBookDoc(entry.bookId)
            val bookSnapshot = transaction.get(bookDoc)
            updateEntryInfo(transaction, bookSnapshot, entry)
            updateBookAsset(transaction, bookSnapshot, entry)
            null
        }
            .addOnSuccessListener {
                operator.execute(null)
            }
            .addOnFailureListener { e ->
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
                    val entries = DocumentConverter.toObjects(querySnapshot, Entry::class.java)
                    operator.execute(entries)
                }
            }
    }

    fun getEntryDoc(id: String) = collection.document(id)

    internal inner class CollectionBinder : Binder() {
        fun getService(): EntryService {
            val service = this@EntryService
            service.db = FirebaseFirestore.getInstance()
            service.collection = db.collection(Constants.COLLECTION_ENTRY)
            service.bookService = ForExMgtApp.bookService
            return service
        }
    }
}