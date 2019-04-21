package com.vincent.forexmgt

import org.apache.commons.lang3.StringUtils

enum class EntryType(
    val twdAmtLabelRes: Int,
    val fcyColorRes: Int) {

    CREDIT(R.string.label_twd_value, R.color.amount_credit),
    DEBIT(R.string.label_amount_and_profit, R.color.amount_debit),
    BALANCE(R.string.label_pv_and_profit, R.color.amount_balance);

    companion object {

        fun fromName(name: String?): EntryType? {
            return values().firstOrNull { type ->
                StringUtils.equalsIgnoreCase(type.name, name)
            }
        }

    }
}