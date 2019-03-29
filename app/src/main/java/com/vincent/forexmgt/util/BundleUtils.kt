package com.vincent.forexmgt.util

import android.os.Bundle

class BundleUtils {

    companion object {

        fun getBundle(key: String, value: String): Bundle {
            val bundle = Bundle()
            bundle.putString(key, value)
            return bundle
        }

    }

}