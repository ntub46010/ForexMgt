package com.vincent.forexmgt.entity

data class BookAssetReport(
    var bookName: String,
    var fcyAmt: Double,
    var twdPV: Int,
    var twdCost: Int)