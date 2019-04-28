package com.vincent.forexmgt.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.vincent.forexmgt.CurrencyType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.BookAssetReport
import com.vincent.forexmgt.entity.CurrencyAssetReport
import com.vincent.forexmgt.util.FormatUtils

class AssetSummaryAdapter(
    private val context: Context,
    private var currencyReportList: List<CurrencyAssetReport>,
    private var bookReportsMap: Map<CurrencyType, List<BookAssetReport>>)
    : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): Any {
        return currencyReportList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val currencyType = currencyReportList[groupPosition].currencyType
        return bookReportsMap[currencyType]!![childPosition]
    }

    override fun getGroupCount(): Int {
        return currencyReportList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val currencyType = currencyReportList[groupPosition].currencyType
        return bookReportsMap[currencyType]!!.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return groupPosition * 100 + childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_general_book_asset, null)
        val imgIndicator = view.findViewById<ImageView>(R.id.imgIndicator)
        val imgCurrency = view.findViewById<ImageView>(R.id.imgCurrency)
        val txtCurrencyName = view.findViewById<TextView>(R.id.txtCurrencyName)
        val txtFcyAmt = view.findViewById<TextView>(R.id.txtFcyAmt)
        val txtFcyType = view.findViewById<TextView>(R.id.txtFcyType)
        val txtTwdPV = view.findViewById<TextView>(R.id.txtTwdPV)
        val txtAvgCost = view.findViewById<TextView>(R.id.txtAvgCostValue)

        val summary = currencyReportList[groupPosition]
        val currencyType = summary.currencyType!!

        imgCurrency.setImageResource(currencyType.iconRes)
        txtCurrencyName.text = currencyType.chineseName
        txtFcyAmt.text = FormatUtils.formatMoney(summary.fcyAmt)
        txtFcyType.text = currencyType.name
        txtTwdPV.text = FormatUtils.formatMoney(summary.twdPV)
        txtAvgCost.text = FormatUtils.formatExchangeRate(summary.avgCost)

        // #E9BC5D
        if (isExpanded) {
            imgIndicator.setImageResource(R.drawable.icon_arrow_circle_up)
        } else {
            imgIndicator.setImageResource(R.drawable.icon_arrow_circle_down)
        }

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_sub_book_asset, null)
        val txtBookName = view.findViewById<TextView>(R.id.txtBookName)
        val txtFcyAmt = view.findViewById<TextView>(R.id.txtFcyAmt)
        val txtFcyType = view.findViewById<TextView>(R.id.txtFcyType)
        val txtTwdAmt = view.findViewById<TextView>(R.id.txtTwdAmt)

        val currencyType = currencyReportList[groupPosition].currencyType!!
        val summary = bookReportsMap[currencyType]!![childPosition]

        txtBookName.text = summary.bookName
        txtFcyAmt.text = FormatUtils.formatMoney(summary.fcyAmt)
        txtFcyType.text = currencyType.name

        val strTwdPV = FormatUtils.formatMoney(summary.twdPV)
        val strTwdCost = FormatUtils.formatMoney(summary.twdCost)
        txtTwdAmt.text = context.getString(R.string.template_slash_with_2_string, strTwdPV, strTwdCost)

        return view
    }

    fun refreshData(currencyReportList: List<CurrencyAssetReport>, bookReportsMap: Map<CurrencyType, List<BookAssetReport>>) {
        this.currencyReportList = currencyReportList
        this.bookReportsMap = bookReportsMap
        notifyDataSetChanged()
    }

}