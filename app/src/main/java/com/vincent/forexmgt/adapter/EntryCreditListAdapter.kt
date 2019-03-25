package com.vincent.forexmgt.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.EntryCredit
import java.text.SimpleDateFormat
import java.util.*

class EntryCreditListAdapter(
    private val context: Context,
    var entries: List<EntryCredit>)
    : BaseAdapter() {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN)

    override fun getItem(position: Int): Any {
        return entries[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return entries.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?:
            LayoutInflater.from(context).inflate(R.layout.item_entry_credit, parent, false)

        val txtDate = view.findViewById<TextView>(R.id.txtDate)
        val txtFcyAmt = view.findViewById<TextView>(R.id.txtFcyAmt)
        val txtFcyType = view.findViewById<TextView>(R.id.txtFcyType)
        val txtTwdAmt = view.findViewById<TextView>(R.id.txtTwdAmt)
        val txtExRateValue = view.findViewById<TextView>(R.id.txtExRateValue)

        val entry = entries[position]
        txtDate.text = dateFormat.format(entry.createdTime)
        txtFcyAmt.text = entry.fcyAmt.toString()
        txtFcyType.text = entry.fcyType
        txtTwdAmt.text = entry.twdAmt.toString()
        txtExRateValue.text = entry.exchangeRate.toString()

        return view
    }

}