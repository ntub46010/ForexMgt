package com.vincent.forexmgt.entity

import com.vincent.forexmgt.CurrencyType
import java.io.Serializable
import java.util.*

data class Book(
    var name: String = "",
    var currencyType: CurrencyType? = null,
    var creator: String = "",
    var createdTime: Date? = null,
    var twdTotalCost: Int = 0,
    var fcyTotalAmt: Double = 0.0)
    : Serializable {

    private var id: String = ""

    fun obtainId() = id

    fun defineId(id: String) {
        this.id = id
    }

}