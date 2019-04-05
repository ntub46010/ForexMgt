package com.vincent.forexmgt.util

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import java.io.Serializable

class BundleBuilder {

    private val bundle = Bundle()

    fun putString(key: String, value: String): BundleBuilder {
        bundle.putString(key, value)
        return this
    }

    fun putSerializable(key: String, value: Serializable): BundleBuilder {
        bundle.putSerializable(key, value)
        return this
    }

    fun build(): Bundle {
        return bundle
    }

    fun appendToIntent(intent: Intent): Intent {
        intent.putExtras(bundle)
        return intent
    }

    fun appendToFragment(fragment: Fragment): Fragment {
        fragment.arguments = bundle
        return fragment
    }

}