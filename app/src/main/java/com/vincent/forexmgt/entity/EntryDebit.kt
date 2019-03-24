package com.vincent.forexmgt.entity

import com.vincent.forexmgt.Entity
import java.util.*

data class EntryDebit(
    override var id: String,
    var bookId: String,
    var createdTime: Date? = null,
    var fcyType: String,
    var fcyAmt: Double,
    var twdAmt: Int,
    var twdBV: Int,
    var exchangeRate: Double)
    : Entity(id)