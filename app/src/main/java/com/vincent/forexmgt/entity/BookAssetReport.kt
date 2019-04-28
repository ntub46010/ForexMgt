package com.vincent.forexmgt.entity

data class BookAssetReport(
    var bookId: String,
    var bookName: String,
    var fcyAmt: Double,
    var twdPV: Int,
    var twdCost: Int)