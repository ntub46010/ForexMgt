package com.vincent.forexmgt.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Entry
import com.vincent.forexmgt.entity.EntryBalance
import com.vincent.forexmgt.entity.EntryCredit
import com.vincent.forexmgt.entity.EntryDebit
import java.text.SimpleDateFormat
import java.util.*

class EntryListAdapter(
    val context: Context,
    var entries: List<Entry>,
    val entryType: EntryType)
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
            LayoutInflater.from(context).inflate(R.layout.item_entry, parent, false)

        val txtDate = view.findViewById<TextView>(R.id.txtDate)
        val txtFcyAmt = view.findViewById<TextView>(R.id.txtFcyAmt)
        val txtFcyType = view.findViewById<TextView>(R.id.txtFcyType)
        val txtTwdAmtLabel = view.findViewById<TextView>(R.id.txtTwdAmtLabel)
        val txtTwdAmt = view.findViewById<TextView>(R.id.txtTwdAmt)
        val txtExRateValue = view.findViewById<TextView>(R.id.txtExRateValue)

        val entry = entries[position]
        txtDate.text = dateFormat.format(entry.createdTime)
        txtFcyAmt.text = entry.fcyAmt.toString()
        txtFcyAmt.setTextColor(context.resources.getColor(entryType.fcyColorRes))
        txtFcyType.text = entry.fcyType
        txtFcyType.setTextColor(context.resources.getColor(entryType.fcyColorRes))
        txtTwdAmtLabel.text = context.getString(entryType.twdAmtLabelRes)
        txtExRateValue.text = entry.exchangeRate.toString()

        txtTwdAmt.text =
            when(entry) {
                is EntryCredit -> entry.twdAmt.toString()
                is EntryDebit -> context.getString(R.string.template_slash_with_2_decimal, entry.twdAmt, entry.twdBV)
                is EntryBalance -> context.getString(R.string.template_slash_with_2_decimal,
                    entry.twdAmt, (entry.twdProfit))
                else -> ""
            }

        return view
    }

    private fun formatProfitText(profit: Int): String {
        return when {
            profit > 0 -> profit.toString()
            profit < 0 -> profit.toString()
            else -> profit.toString()
        }
    }
}