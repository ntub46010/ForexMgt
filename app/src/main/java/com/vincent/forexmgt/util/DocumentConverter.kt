package com.vincent.forexmgt.util

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.vincent.forexmgt.entity.Entity

class DocumentConverter {

    companion object {

        fun <T: Entity> toObject(documentSnapshot: DocumentSnapshot?, clz: Class<T>): T {
            return documentSnapshot?.toObject(clz)!!
        }

        fun <T: Entity> toObjects(querySnapshot: QuerySnapshot?, clz: Class<T>): List<T> {
            return querySnapshot!!.toObjects(clz).toList()
        }
    }

}