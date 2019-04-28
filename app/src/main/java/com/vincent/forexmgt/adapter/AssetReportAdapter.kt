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

class AssetReportAdapter(
    private val context: Context,
    private var currencyReports: List<CurrencyAssetReport>,
    private var currencyToBookReportsMap: Map<CurrencyType, List<BookAssetReport>>)
    : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): Any {
        return currencyReports[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val currencyType = currencyReports[groupPosition].currencyType
        return currencyToBookReportsMap[currencyType]!![childPosition]
    }

    override fun getGroupCount(): Int {
        return currencyReports.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val currencyType = currencyReports[groupPosition].currencyType
        return currencyToBookReportsMap[currencyType]!!.size
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
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_asset_currency, null)
        val imgIndicator = view.findViewById<ImageView>(R.id.imgIndicator)
        val imgCurrency = view.findViewById<ImageView>(R.id.imgCurrency)
        val txtCurrencyName = view.findViewById<TextView>(R.id.txtCurrencyName)
        val txtFcyAmt = view.findViewById<TextView>(R.id.txtFcyAmt)
        val txtFcyType = view.findViewById<TextView>(R.id.txtFcyType)
        val txtTwdPV = view.findViewById<TextView>(R.id.txtTwdPV)
        val txtAvgCost = view.findViewById<TextView>(R.id.txtAvgCostValue)

        val currencyReport = currencyReports[groupPosition]
        val currencyType = currencyReport.currencyType!!

        imgCurrency.setImageResource(currencyType.iconRes)
        txtCurrencyName.text = currencyType.chineseName
        txtFcyAmt.text = FormatUtils.formatMoney(currencyReport.fcyAmt)
        txtFcyType.text = currencyType.name
        txtTwdPV.text = FormatUtils.formatMoney(currencyReport.twdPV)
        txtAvgCost.text = FormatUtils.formatExchangeRate(currencyReport.avgCost)

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
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_asset_book, null)
        val txtBookName = view.findViewById<TextView>(R.id.txtBookName)
        val txtFcyAmt = view.findViewById<TextView>(R.id.txtFcyAmt)
        val txtFcyType = view.findViewById<TextView>(R.id.txtFcyType)
        val txtTwdAmt = view.findViewById<TextView>(R.id.txtTwdAmt)

        val currencyType = currencyReports[groupPosition].currencyType!!
        val bookReport = currencyToBookReportsMap[currencyType]!![childPosition]

        txtBookName.text = bookReport.bookName
        txtFcyAmt.text = FormatUtils.formatMoney(bookReport.fcyAmt)
        txtFcyType.text = currencyType.name

        val strTwdPV = FormatUtils.formatMoney(bookReport.twdPV)
        val strTwdCost = FormatUtils.formatMoney(bookReport.twdCost)
        txtTwdAmt.text = context.getString(R.string.template_slash_with_2_string, strTwdPV, strTwdCost)

        return view
    }

    fun refreshData(currencyReportList: List<CurrencyAssetReport>, bookReportsMap: Map<CurrencyType, List<BookAssetReport>>) {
        this.currencyReports = currencyReportList
        this.currencyToBookReportsMap = bookReportsMap
        notifyDataSetChanged()
    }

}