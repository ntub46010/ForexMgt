package com.vincent.forexmgt.util

import android.os.Bundle
import java.io.Serializable

class BundleUtils {

    companion object {

        fun getBundle(key: String, value: String): Bundle {
            val bundle = Bundle()
            bundle.putString(key, value)
            return bundle
        }

        fun getBundle(key: String, obj: Serializable): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(key, obj)
            return bundle
        }

    }

}