package com.vincent.forexmgt.entity

import com.vincent.forexmgt.Entity
import java.util.*

data class Book(
    override var id: String = "",
    var name: String = "",
    var currencyType: String = "",
    var creator: String = "",
    var createdTime: Date? = null)
    : Entity(id)