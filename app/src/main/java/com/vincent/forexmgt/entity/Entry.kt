package com.vincent.forexmgt.entity

import com.vincent.forexmgt.CurrencyType
import com.vincent.forexmgt.EntryType
import java.util.*

class Entry(
    var bookId: String = "",
    var createdTime: Date? = null,
    val type: EntryType? = null,
    var currencyType: CurrencyType? = null,
    var fcyAmt: Double = 0.0,
    var twdAmt: Int = 0,
    var exchangeRate: Double = 0.0,
    var twdCost: Int = 0) {

    private var id: String = ""

    fun obtainId() = id

    fun defineId(id: String) {
        this.id = id
    }

}