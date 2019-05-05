package com.vincent.forexmgt.service

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.vincent.forexmgt.Bank
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.CurrencyType
import com.vincent.forexmgt.entity.ExchangeRate
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import java.io.Serializable

class LoadingExchangeRateService : IntentService("LoadingExchangeRateService") {

    override fun onHandleIntent(intent: Intent?) {
        val receiver = intent?.getParcelableExtra(Constants.KEY_RECEIVER) as ResultReceiver
        val bankName = intent.getStringExtra(Constants.KEY_BANK_NAME) ?: Bank.FUBON.name
        val bank = Bank.valueOf(bankName)

        try {
            val response = Jsoup.connect(bank.exchangeRateUrl).execute()

            val webContent = response.body()
            val tableRows = Jsoup.parse(webContent)
                .select("div[id=right]")
                .select("table>tbody>tr")

            tableRows.dropLast(2)
            tableRows.removeAt(0)

            val rates = mutableListOf<ExchangeRate>()

            for (row in tableRows) {
                val tableCells = row.select("td")
                val currencyCode = StringUtils.split(tableCells[0].select("a").text(), StringUtils.SPACE)[1]
                val currencyType = CurrencyType.fromCode(currencyCode)

                rates.add(
                    ExchangeRate(
                        currencyType,
                        tableCells[4].text().toDouble(),
                        tableCells[3].text().toDouble()
                    )
                )
            }

            val responseRates = postProcess(bank, rates)

            val bundle = Bundle()
            bundle.putSerializable(Constants.KEY_DATA, responseRates as Serializable)

            receiver.send(0, bundle)
        } catch (e: Exception) {
            val bundle = Bundle()
            bundle.putSerializable(Constants.KEY_DATA, e as Serializable)
            receiver.send(0, bundle)
        }

    }

    private fun postProcess(bank: Bank, rates: List<ExchangeRate>): List<ExchangeRate> {
        var newRates = rates

        if (bank == Bank.RICHART) {
            newRates = rates.filter { r -> r.currencyType != CurrencyType.THB }
            newRates = calcRichartRate(newRates)
        }

        return newRates
    }

    private fun calcRichartRate(rates: List<ExchangeRate>): List<ExchangeRate> {
        for (rate in rates) {
            when (rate.currencyType) {
                CurrencyType.USD -> {
                    rate.credit -= Constants.RICHART_DISCOUNT_USD
                    rate.debit += Constants.RICHART_DISCOUNT_USD
                }
                CurrencyType.JPY -> {
                    rate.credit -= Constants.RICHART_DISCOUNT_JPY
                    rate.debit += Constants.RICHART_DISCOUNT_JPY
                }
                CurrencyType.GBP -> {
                    rate.credit -= Constants.RICHART_DISCOUNT_GBP
                    rate.debit += Constants.RICHART_DISCOUNT_GBP
                }
                CurrencyType.CNY -> {
                    rate.credit -= Constants.RICHART_DISCOUNT_CNY
                    rate.debit += Constants.RICHART_DISCOUNT_CNY
                }
                CurrencyType.EUR -> {
                    rate.credit -= Constants.RICHART_DISCOUNT_EUR
                    rate.debit += Constants.RICHART_DISCOUNT_EUR
                }
                CurrencyType.HKD -> {
                    rate.credit -= Constants.RICHART_DISCOUNT_HKD
                    rate.debit += Constants.RICHART_DISCOUNT_HKD
                }
                CurrencyType.AUD -> {
                    rate.credit -= Constants.RICHART_DISCOUNT_AUD
                    rate.debit += Constants.RICHART_DISCOUNT_AUD
                }
                else -> {
                    val discount = (rate.credit - rate.debit) / 5
                    rate.credit -= discount
                    rate.debit += discount
                }
            }
        }

        return rates
    }

}