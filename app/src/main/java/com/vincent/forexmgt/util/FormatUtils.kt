package com.vincent.forexmgt.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FormatUtils {

    companion object {

        private val moneyFormat = NumberFormat.getNumberInstance(Locale.US)
        private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN)
        private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.TAIWAN)

        fun formatMoney(amount: Int): String = moneyFormat.format(amount)!!

        fun formatMoney(amount: Double): String = moneyFormat.format(amount)!!

        fun formatExchangeRate(rate: Double): String = java.lang.String.format(Locale.US, "%.4f", rate)

        fun formatDate(date: Date?): String = dateFormat.format(date)

        fun formatDateTime(date: Date?): String = dateTimeFormat.format(date)

        fun parseDate(dateStr: String): Date = dateFormat.parse(dateStr)

        fun calcExchangeRate(twdAmt: Int, fcyAmt: Double): Double = Math.round(twdAmt * 10000 / fcyAmt) / 10000.0

    }

}