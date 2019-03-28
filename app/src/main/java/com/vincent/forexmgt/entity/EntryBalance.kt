package com.vincent.forexmgt.entity

import java.util.*

class EntryBalance(
    id: String,
    bookId: String,
    createdTime: Date? = null,
    fcyType: String,
    fcyAmt: Double,
    twdAmt: Int,
    exchangeRate: Double,
    var twdProfit: Int)
    : Entry(id, bookId, createdTime, fcyType, fcyAmt, twdAmt, exchangeRate)