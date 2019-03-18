package com.vincent.forexmgt.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.Operator
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Book
import org.apache.commons.lang3.StringUtils

class BookService : Service() {

    private lateinit var collection: CollectionReference
    private lateinit var currentLoginUser: FirebaseUser

    override fun onBind(intent: Intent?): IBinder {
        return CollectionBinder()
    }

    fun createBook(book: Book) {
        if (StringUtils.isEmpty(book.creator)) {
            book.creator = currentLoginUser.uid
        }

        collection
            .add(book)
            .addOnSuccessListener {
                Toast.makeText(this@BookService, getString(R.string.create_successfully), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@BookService, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadBooks(operator: Operator) {
        collection
            .whereEqualTo(Constants.PROPERTY_CREATOR, currentLoginUser.uid)
            .orderBy(Constants.PROPERTY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                val books = querySnapshot?.toObjects(Book::class.java)?.toList()!!
                operator.execute(books)
            }
    }

    internal inner class CollectionBinder : Binder() {
        fun getService(): BookService {
            val service: BookService = this@BookService
            service.collection = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_BOOK)
            service.currentLoginUser = FirebaseAuth.getInstance().currentUser!!

            return service
        }
    }

}