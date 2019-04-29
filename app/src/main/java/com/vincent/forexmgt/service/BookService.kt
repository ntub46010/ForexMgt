package com.vincent.forexmgt.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.vincent.forexmgt.Callback
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.ForExMgtApp
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.util.DocumentConverter
import org.apache.commons.lang3.StringUtils

class BookService : Service() {

    private lateinit var db: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private var currentLoginUser: FirebaseUser? = null

    override fun onBind(intent: Intent?): IBinder {
        return CollectionBinder()
    }

    fun createBook(book: Book, callback: Callback<Book>) {
        if (StringUtils.isEmpty(book.creator)) {
            book.creator = currentLoginUser?.uid!!
        }

        collection
            .add(book)
            .addOnSuccessListener { documentRef ->
                book.defineId(documentRef.id)
                callback.onExecute(book)
            }
            .addOnFailureListener { e ->
                callback.onError(e)
            }
    }

    fun subscribeBook(id: String, callback: Callback<Book>): ListenerRegistration {
        return collection
            .document(id)
            .addSnapshotListener { documentSnapshot, exception ->
                if (exception != null) {
                    callback.onError(exception)
                } else {
                    val book = DocumentConverter.toBook(documentSnapshot)
                    callback.onExecute(book)
                }
            }
    }

    fun loadBooks(callback: Callback<List<Book>>) {
        collection
            .whereEqualTo(Constants.PROPERTY_CREATOR, currentLoginUser?.uid)
            .orderBy(Constants.PROPERTY_CREATED_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val books = DocumentConverter.toBooks(querySnapshot)
                callback.onExecute(books)
            }
            .addOnFailureListener { e ->
                callback.onError(e)
            }
    }

    fun getBookDoc(id: String) = collection.document(id)

    fun setLoginUser(currentLoginUser: FirebaseUser?) {
        this.currentLoginUser = currentLoginUser
    }

    internal inner class CollectionBinder : Binder() {
        fun getService(): BookService {
            val service: BookService = this@BookService
            service.db = FirebaseFirestore.getInstance()
            service.collection = db.collection(Constants.COLLECTION_BOOK)
            service.currentLoginUser = ForExMgtApp.currentLoginUser

            return service
        }
    }

}