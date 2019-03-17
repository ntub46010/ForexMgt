package com.vincent.forexmgt

import org.apache.commons.lang3.StringUtils

enum class CurrencyType(
    val title: String,
    val iconRes: Int) {
    // https://www.flaticon.com/free-icon/question_189665#term=question&page=1&position=8
    // https://www.flaticon.com/free-icon/question_391038#term=question&page=4&position=57

    USD("美金 USD", R.drawable.flag_usd),
    CNY("人民幣 CNY", R.drawable.flag_cny),
    JPY("日幣 JPY", R.drawable.flag_jpy),
    EUR("歐元 EUR", R.drawable.flag_eur),
    HKD("港幣 HKD", R.drawable.flag_hkd),
    AUD("澳幣 AUD", R.drawable.flag_aud),
    ZAR("南非幣 ZAR", R.drawable.flag_zar),
    CAD("加拿大幣 CAD", R.drawable.flag_cad),
    GBP("英鎊 GBP", R.drawable.flag_gbp),
    SGD("新加坡幣 SGD", R.drawable.flag_sgd),
    CHF("瑞士法郎 CHF", R.drawable.flag_chf),
    NZD("紐元 NZD", R.drawable.flag_nzd),
    SEK("瑞典幣 SEK", R.drawable.flag_sek),
    THB("泰幣 THB", R.drawable.flag_thb);

    companion object {

        fun fromTitle(title: String): CurrencyType? {
            return values().firstOrNull { type ->
                StringUtils.equals(type.title, title)
            }
        }

        fun fromTitleContains(keyword: String): CurrencyType? {
            return values().firstOrNull { type ->
                StringUtils.containsIgnoreCase(type.title, keyword)
            }
        }

    }


}