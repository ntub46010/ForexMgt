package com.vincent.forexmgt.entity

import com.vincent.forexmgt.Entity
import com.vincent.forexmgt.EntryType
import java.util.*

class Entry(
    id: String,
    var bookId: String = "",
    var createdTime: Date? = null,
    val type: EntryType? = null,
    var fcyType: String = "",
    var fcyAmt: Double = 0.0,
    var twdAmt: Int = 0,
    var exchangeRate: Double = 0.0,
    var twdCost: Int? = null,
    var twdBV: Int? = null,
    var twdProfit: Int? = null)
    : Entity(id) {

}