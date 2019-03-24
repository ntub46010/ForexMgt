package com.vincent.forexmgt

import org.apache.commons.lang3.StringUtils

enum class EntryType {

    CREDIT, DEBIT, BALANCE;

    companion object {

        fun fromName(name: String?): EntryType? {
            return values().firstOrNull { type ->
                StringUtils.equalsIgnoreCase(type.name, name)
            }
        }

    }
}