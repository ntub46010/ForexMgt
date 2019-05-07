package com.vincent.forexmgt

import org.apache.commons.lang3.StringUtils

enum class Bank(
    val chineseName: String,
    val exchangeRateUrl: String) {

    FUBON("富邦銀行", "https://www.findrate.tw/bank/8/#.XHv2PKBS8dU"),
    TAISHIN("台新銀行", "https://www.findrate.tw/bank/9/#.XM5YhthS8dU"),
    RICHART("台新銀行（Richart）", "https://www.findrate.tw/bank/9/#.XM5YhthS8dU"); // Richart沒有泰幣

    companion object {

        fun fromChineseName(chineseName: String?): Bank? {
            return values().firstOrNull {
                StringUtils.equals(it.chineseName, chineseName)
            }
        }

        fun getChineseNames(): List<String> {
            val names = mutableListOf<String>()

            for (bank in values()) {
                names.add(bank.chineseName)
            }

            return names
        }

    }

}