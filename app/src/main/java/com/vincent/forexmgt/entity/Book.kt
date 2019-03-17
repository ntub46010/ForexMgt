package com.vincent.forexmgt.entity

import java.util.*

data class Book(
    var name: String = "",
    var currencyType: String = "",
    var creator: String = "",
    var createdTime: Date? = null
)