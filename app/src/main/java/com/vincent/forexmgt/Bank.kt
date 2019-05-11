package com.vincent.forexmgt

import org.apache.commons.lang3.StringUtils

enum class Bank(
    val chineseName: String,
    val exchangeRateUrl: String,
    private val supportCurrency: List<CurrencyType>) {

    FUBON("富邦銀行", "https://www.findrate.tw/bank/8/#.XHv2PKBS8dU",
        listOf(CurrencyType.USD, CurrencyType.CNY, CurrencyType.JPY, CurrencyType.EUR, CurrencyType.HKD, CurrencyType.AUD,
            CurrencyType.ZAR, CurrencyType.CAD, CurrencyType.GBP, CurrencyType.SGD, CurrencyType.CHF, CurrencyType.NZD,
            CurrencyType.SEK, CurrencyType.THB)),

    TAISHIN("台新銀行", "https://www.findrate.tw/bank/9/#.XM5YhthS8dU",
        listOf(CurrencyType.USD, CurrencyType.CNY, CurrencyType.JPY, CurrencyType.EUR, CurrencyType.HKD, CurrencyType.AUD,
            CurrencyType.ZAR, CurrencyType.CAD, CurrencyType.GBP, CurrencyType.SGD, CurrencyType.CHF, CurrencyType.NZD,
            CurrencyType.SEK, CurrencyType.THB)),

    RICHART("台新銀行（Richart）", "https://www.findrate.tw/bank/9/#.XM5YhthS8dU",
        listOf(CurrencyType.USD, CurrencyType.CNY, CurrencyType.JPY, CurrencyType.EUR, CurrencyType.HKD, CurrencyType.AUD,
            CurrencyType.ZAR, CurrencyType.CAD, CurrencyType.GBP, CurrencyType.SGD, CurrencyType.CHF, CurrencyType.NZD,
            CurrencyType.SEK));

    fun getSupportCurrencyTitles(): List<String> {
        val titles = mutableListOf<String>()

        val currencyTypes = supportCurrency.sortedBy { it.ordinal }
        for (currencyType in currencyTypes) {
            titles.add("${currencyType.chineseName}${StringUtils.SPACE}${currencyType.name}")
        }

        return titles
    }

    companion object {

        fun fromChineseName(chineseName: String?): Bank? {
            return values().firstOrNull {
                StringUtils.equals(it.chineseName, chineseName)
            }
        }

        fun getChineseTitles(): List<String> {
            val titles = mutableListOf<String>()

            for (bank in values()) {
                titles.add(bank.chineseName)
            }

            return titles
        }

    }

}