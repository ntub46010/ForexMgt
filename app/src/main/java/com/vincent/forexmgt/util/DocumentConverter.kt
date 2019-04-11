package com.vincent.forexmgt.util

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.entity.Entry

class DocumentConverter {

    companion object {

        fun toBook(documentSnapshot: DocumentSnapshot?): Book {
            val book = documentSnapshot?.toObject(Book::class.java)!!
            book.defineId(documentSnapshot.id)

            return book
        }

        fun toBooks(querySnapshot: QuerySnapshot?): List<Book> {
            val books = mutableListOf<Book>()

            for (document in querySnapshot!!.documents) {
                val book = document.toObject(Book::class.java)!!
                book.defineId(document.id)

                books.add(book)
            }

            return books
        }

        fun toEntries(querySnapshot: QuerySnapshot?): List<Entry> {
            val entries = mutableListOf<Entry>()

            for (document in querySnapshot!!.documents) {
                val entry = document.toObject(Entry::class.java)!!
                entry.defineId(document.id)

                entries.add(entry)
            }

            return entries
        }

    }

}