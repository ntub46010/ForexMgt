package com.vincent.forexmgt.entity

import com.vincent.forexmgt.CurrencyType

data class GeneralAssetSummary(
    var currencyType: CurrencyType?,
    var fcyAmt: Double,
    var twdPV: Int,
    var avgCost: Double)