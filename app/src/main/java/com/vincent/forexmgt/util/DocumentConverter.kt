package com.vincent.forexmgt.util

import com.google.firebase.firestore.QuerySnapshot
import com.vincent.forexmgt.entity.Entity

class DocumentConverter {

    companion object {
        fun <T: Entity> toObject(querySnapshot: QuerySnapshot?, clz: Class<T>): List<T> {
            val results = mutableListOf<T>()

            for (document in querySnapshot?.documents!!) {
                val obj = document.toObject(clz)!!
                obj.id = document.id
                results.add(obj)
            }

            return results
        }
    }

}