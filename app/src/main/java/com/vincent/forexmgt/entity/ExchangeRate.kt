package com.vincent.forexmgt.entity

import com.vincent.forexmgt.CurrencyType
import java.io.Serializable

data class ExchangeRate(
    var currencyType: CurrencyType?,
    var credit: Double,
    var debit: Double)
    : Serializable