package com.vincent.forexmgt.util

import android.app.Dialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.Window
import com.vincent.forexmgt.R

class DialogUtils {

    companion object {

        fun getPlainDialog(context: Context, title: String, message: String) =
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("確定", null)

        fun getWaitingDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_waiting)
            dialog.setCancelable(false)
            return dialog
        }

    }

}