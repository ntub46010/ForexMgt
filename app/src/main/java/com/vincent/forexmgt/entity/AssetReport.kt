package com.vincent.forexmgt.entity

import com.vincent.forexmgt.CurrencyType
import java.io.Serializable

data class AssetReport(
    val currencyReports: List<CurrencyAssetReport>,
    val currencyToBookReportsMap: Map<CurrencyType, List<BookAssetReport>>)
    : Serializable