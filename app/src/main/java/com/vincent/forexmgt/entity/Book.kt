package com.vincent.forexmgt.entity

import com.vincent.forexmgt.CurrencyType
import java.io.Serializable
import java.util.*

data class Book(
    override var id: String = "",
    var name: String = "",
    var currencyType: CurrencyType? = null,
    var creator: String = "",
    var createdTime: Date? = null)
    : Entity(id), Serializable