package com.vincent.forexmgt.entity

import com.vincent.forexmgt.EntryType
import java.util.*

class Entry(
    id: String,
    var bookId: String = "",
    var createdTime: Date? = null,//
    val type: EntryType? = null,
    var fcyType: String = "",
    var fcyAmt: Double = 0.0,//
    var twdAmt: Int = 0,//
    var exchangeRate: Double = 0.0,
    var twdCost: Int? = null, // 勾選方塊
    var twdBV: Int? = null, // 臺幣總成本 * ( 領出外幣 / 總外幣 )
    var twdProfit: Int? = null) // 臺幣現值 - 臺幣總成本
    : Entity(id) {

}