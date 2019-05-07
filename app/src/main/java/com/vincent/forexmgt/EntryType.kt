package com.vincent.forexmgt

enum class EntryType(
    val twdAmtLabelRes: Int,
    val fcyColorRes: Int) {

    CREDIT(R.string.label_twd_value, R.color.amount_credit),
    DEBIT(R.string.label_amount_and_profit, R.color.amount_debit),
    BALANCE(R.string.label_pv_and_profit, R.color.amount_balance);

}