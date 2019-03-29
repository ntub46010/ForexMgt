package com.vincent.forexmgt.entity

import com.vincent.forexmgt.Entity
import java.util.*

abstract class Entry(
    id: String,
    open var bookId: String,
    open var createdTime: Date? = null,
    open var fcyType: String,
    open var fcyAmt: Double,
    open var twdAmt: Int,
    open var exchangeRate: Double)
    : Entity (id)