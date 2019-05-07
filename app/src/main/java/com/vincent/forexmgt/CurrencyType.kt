package com.vincent.forexmgt

import org.apache.commons.lang3.StringUtils

enum class CurrencyType(
    val order: Int,
    val chineseName: String,
    val iconRes: Int) {
    // https://www.flaticon.com/free-icon/question_189665#term=question&page=1&position=8
    // https://www.flaticon.com/free-icon/question_391038#term=question&page=4&position=57

    USD(1, "美金", R.drawable.flag_usd),
    CNY(2, "人民幣", R.drawable.flag_cny),
    JPY(3, "日幣", R.drawable.flag_jpy),
    EUR(4, "歐元", R.drawable.flag_eur),
    HKD(5, "港幣", R.drawable.flag_hkd),
    AUD(6, "澳幣", R.drawable.flag_aud),
    ZAR(7, "南非幣", R.drawable.flag_zar),
    CAD(8, "加拿大幣", R.drawable.flag_cad),
    GBP(9, "英鎊", R.drawable.flag_gbp),
    SGD(10, "新加坡幣", R.drawable.flag_sgd),
    CHF(11, "瑞士法郎", R.drawable.flag_chf),
    NZD(12, "紐元", R.drawable.flag_nzd),
    SEK(13, "瑞典幣", R.drawable.flag_sek),
    THB(14, "泰幣", R.drawable.flag_thb);

    fun getTitle(): String {
        return "$chineseName${StringUtils.SPACE}$name"
    }

    companion object {

        fun getTitles(): List<String> {
            val titles = mutableListOf<String>()

            for (type in values()) {
                titles.add("${type.chineseName}${StringUtils.SPACE}${type.name}")
            }

            return titles
        }

    }

}