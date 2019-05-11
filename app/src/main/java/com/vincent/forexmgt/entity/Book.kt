package com.vincent.forexmgt.entity

import com.vincent.forexmgt.Bank
import com.vincent.forexmgt.CurrencyType
import java.io.Serializable
import java.util.*

data class Book(
    var name: String = "",
    var bank: Bank? = null,
    var currencyType: CurrencyType? = null,
    var creator: String = "",
    var createdTime: Date? = null)
    : Serializable {

    private var id: String = ""

    fun obtainId() = id

    fun defineId(id: String) {
        this.id = id
    }

}