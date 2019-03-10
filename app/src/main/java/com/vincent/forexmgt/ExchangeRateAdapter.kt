package com.vincent.forexmgt

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class ExchangeRateAdapter(private val exchangeRates: List<ExchangeRate>) : RecyclerView.Adapter<ExchangeRateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exchange_rate, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return exchangeRates.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rate = exchangeRates.get(position)
        holder.bindValue(rate)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val imgCurrencyIcon = v.findViewById<ImageView>(R.id.imgCurrency)
        private val txtCurrencyName = v.findViewById<TextView>(R.id.txtCurrencyName)
        private val txtCredit = v.findViewById<TextView>(R.id.txtCredit)
        private val txtDebit = v.findViewById<TextView>(R.id.txtDebit)

        fun bindValue(rate: ExchangeRate) {
            imgCurrencyIcon.setImageResource(rate.iconRes)
            txtCurrencyName.text = rate.name
            txtCredit.text = rate.credit.toString()
            txtDebit.text = rate.debit.toString()
        }
    }

}