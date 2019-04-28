package com.vincent.forexmgt.entity

import com.vincent.forexmgt.CurrencyType

data class CurrencyAssetReport(
    var currencyType: CurrencyType?,
    var fcyAmt: Double,
    var twdPV: Int,
    var avgCost: Double)