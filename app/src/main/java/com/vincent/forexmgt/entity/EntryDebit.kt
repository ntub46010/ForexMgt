package com.vincent.forexmgt.entity

import java.util.*

class EntryDebit(
    id: String,
    bookId: String,
    createdTime: Date? = null,
    fcyType: String,
    fcyAmt: Double,
    twdAmt: Int,
    exchangeRate: Double,
    var twdBV: Int)
    : Entry(id, bookId, createdTime, fcyType, fcyAmt, twdAmt, exchangeRate)