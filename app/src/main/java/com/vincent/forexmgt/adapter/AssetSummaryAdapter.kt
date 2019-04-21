package com.vincent.forexmgt.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.SubAssetSummary
import com.vincent.forexmgt.entity.GeneralAssetSummary
import com.vincent.forexmgt.util.FormatUtils

class AssetSummaryAdapter(
    private val context: Context,
    private var generalSummaries: List<GeneralAssetSummary>,
    private var subSummaries: List<List<SubAssetSummary>>)
    : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): Any {
        return generalSummaries[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return subSummaries[groupPosition][childPosition]
    }

    override fun getGroupCount(): Int {
        return generalSummaries.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return subSummaries[groupPosition].size
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

        val summary = generalSummaries[groupPosition]
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

        val summary = subSummaries[groupPosition][childPosition]
        val currencyType = generalSummaries[groupPosition].currencyType!!

        txtBookName.text = summary.bookName
        txtFcyAmt.text = FormatUtils.formatMoney(summary.fcyAmt)
        txtFcyType.text = currencyType.name

        val strTwdPV = FormatUtils.formatMoney(summary.twdPV)
        val strTwdCost = FormatUtils.formatMoney(summary.twdCost)
        txtTwdAmt.text = context.getString(R.string.template_slash_with_2_string, strTwdPV, strTwdCost)

        return view
    }

}