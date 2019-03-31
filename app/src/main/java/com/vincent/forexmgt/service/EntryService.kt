package com.vincent.forexmgt.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Entry

class EntryService : Service() {

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
                updateEntry(entry)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EntryService, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateEntry(entry: Entry) {
        collection
            .document(entry.id)
            .set(entry)
            .addOnSuccessListener {
                Toast.makeText(this@EntryService, getString(R.string.create_successfully), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@EntryService, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

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
            service.collection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_ENTRY)
            return service
        }
    }
}