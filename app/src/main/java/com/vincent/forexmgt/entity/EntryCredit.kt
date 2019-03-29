package com.vincent.forexmgt.entity

import java.util.*

class EntryCredit(
    id: String,
    bookId: String,
    createdTime: Date? = null,
    fcyType: String,
    fcyAmt: Double,
    twdAmt: Int,
    exchangeRate: Double,
    var twdCost: Int)
    : Entry(id, bookId, createdTime, fcyType, fcyAmt, twdAmt, exchangeRate)