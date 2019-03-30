package com.vincent.forexmgt.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class EntryListAdapter(
    val context: Context,
    var entries: List<Entry>)
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
        txtFcyAmt.text = formatMoney(entry.fcyAmt)
        txtFcyAmt.setTextColor(context.resources.getColor(entry.type!!.fcyColorRes))
        txtFcyType.text = entry.fcyType
        txtFcyType.setTextColor(context.resources.getColor(entry.type.fcyColorRes))
        txtTwdAmtLabel.text = context.getString(entry.type.twdAmtLabelRes)
        txtTwdAmt.text = getTwdAmount(entry)
        txtExRateValue.text = entry.exchangeRate.toString()

        return view
    }

    private fun getTwdAmount(entry: Entry): String {
        when(entry.type) {
            EntryType.CREDIT -> {
                return formatMoney(entry.twdAmt.toDouble())
            }

            EntryType.DEBIT -> {
                val twdAmt = formatMoney(entry.twdAmt.toDouble())
                val twdBV = formatMoney(entry.twdBV!!.toDouble())
                return context.getString(R.string.template_slash_with_2_string, twdAmt, twdBV)
            }

            EntryType.BALANCE -> {
                val twdAmt = formatMoney(entry.twdAmt.toDouble())
                val twdProfit = formatMoney(entry.twdProfit!!.toDouble())
                return context.getString(R.string.template_slash_with_2_string, twdAmt, twdProfit)
            }

            else -> return ""
        }
    }

    private fun formatMoney(amount: Double) = NumberFormat.getNumberInstance(Locale.US).format(amount)

}