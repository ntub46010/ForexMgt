package com.vincent.forexmgt.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.ForExMgtApp
import com.vincent.forexmgt.Operator
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

    fun createBook(book: Book, operator: Operator) {
        if (StringUtils.isEmpty(book.creator)) {
            book.creator = currentLoginUser?.uid!!
        }

        collection
            .add(book)
            .addOnSuccessListener { documentRef ->
                book.id = documentRef.id
                createBookPostProcess(book, operator)
            }
            .addOnFailureListener { e ->
                operator.execute(e)
            }
    }

    fun subscribeBook(id: String, operator: Operator): ListenerRegistration {
        return collection
            .document(id)
            .addSnapshotListener { documentSnapshot, exception ->
                if (exception != null) {
                    operator.execute(null)
                } else {
                    val book = DocumentConverter.toObject(documentSnapshot, Book::class.java)
                    operator.execute(book)
                }
            }
    }

    fun loadBooks(operator: Operator) {
        collection
            .whereEqualTo(Constants.PROPERTY_CREATOR, currentLoginUser?.uid)
            .orderBy(Constants.PROPERTY_CREATED_TIME, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val books = DocumentConverter.toObjects(querySnapshot, Book::class.java)
                operator.execute(books)
            }
    }

    private fun createBookPostProcess(book: Book, operator: Operator) {
        collection
            .document(book.id)
            .set(book)
            .addOnSuccessListener {
                operator.execute(null)
            }
            .addOnFailureListener { e ->
                operator.execute(e)
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