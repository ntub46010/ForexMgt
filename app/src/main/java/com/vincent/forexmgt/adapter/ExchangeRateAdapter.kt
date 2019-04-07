package com.vincent.forexmgt.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.ExchangeRate
import java.util.*

class ExchangeRateAdapter(
     var exchangeRates: List<ExchangeRate>)
    : RecyclerView.Adapter<ExchangeRateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exchange_rate, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return exchangeRates.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rate = exchangeRates[position]
        holder.bindValue(rate)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @BindView(R.id.imgCurrency) lateinit var imgCurrencyIcon: ImageView
        @BindView(R.id.txtCurrencyTitle) lateinit var txtCurrencyTitle: TextView
        @BindView(R.id.txtCredit) lateinit var txtCredit: TextView
        @BindView(R.id.txtDebit) lateinit var txtDebit: TextView

        init {
            ButterKnife.bind(this, v)
        }

        fun bindValue(rate: ExchangeRate) {
            imgCurrencyIcon.setImageResource(rate.currencyType!!.iconRes)
            txtCurrencyTitle.text = rate.currencyType!!.getTitle()
            txtCredit.text = formatNumber(rate.credit)
            txtDebit.text = formatNumber(rate.debit)
        }
    }

    private fun formatNumber(num: Double) = java.lang.String.format(Locale.US, "%.4f", num)

}