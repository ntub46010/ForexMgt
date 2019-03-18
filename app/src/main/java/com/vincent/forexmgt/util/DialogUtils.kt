package com.vincent.forexmgt.util

import android.content.Context
import android.support.v7.app.AlertDialog

class DialogUtils {

    companion object {

        fun getPlainDialog(context: Context, title: String, message: String) =
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("確定", null)


    }

}