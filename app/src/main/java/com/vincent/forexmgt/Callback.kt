package com.vincent.forexmgt

import kotlin.Exception

interface Callback<T> {

    fun onExecute(data: T)

    fun onError(e: Exception)

}