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
import com.vincent.forexmgt.util.FormatUtils
import org.apache.commons.lang3.StringUtils

class EntryListAdapter(
    val context: Context,
    var entries: List<Entry>)
    : BaseAdapter() {

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

        txtDate.text =
            if (entry.type == EntryType.BALANCE) FormatUtils.formatDateTime(entry.createdTime)
            else FormatUtils.formatDate(entry.createdTime)

        txtFcyAmt.text = FormatUtils.formatMoney(entry.fcyAmt)
        txtFcyAmt.setTextColor(context.resources.getColor(entry.type!!.fcyColorRes))
        txtFcyType.text = entry.currencyType?.name
        txtFcyType.setTextColor(context.resources.getColor(entry.type.fcyColorRes))
        txtTwdAmtLabel.text = context.getString(entry.type.twdAmtLabelRes)
        txtTwdAmt.text = getTwdAmount(entry)
        txtExRateValue.text = FormatUtils.formatExchangeRate(entry.exchangeRate)

        return view
    }

    private fun getTwdAmount(entry: Entry): String {
        when(entry.type) {
            EntryType.CREDIT -> {
                return FormatUtils.formatMoney(entry.twdAmt)
            }

            EntryType.DEBIT -> {
                val twdAmt = FormatUtils.formatMoney(entry.twdAmt)
                val twdProfit = FormatUtils.formatMoney(entry.twdAmt - entry.twdCost)
                return context.getString(R.string.template_slash_with_2_string, twdAmt, twdProfit)
            }

            EntryType.BALANCE -> {
                val twdAmt = FormatUtils.formatMoney(entry.twdAmt)
                val twdProfit = FormatUtils.formatMoney(entry.twdAmt - entry.twdCost)
                return context.getString(R.string.template_slash_with_2_string, twdAmt, twdProfit)
            }

            else -> return StringUtils.EMPTY
        }
    }

    fun refreshData(entries: List<Entry>) {
        this.entries = entries
        notifyDataSetChanged()
    }
}