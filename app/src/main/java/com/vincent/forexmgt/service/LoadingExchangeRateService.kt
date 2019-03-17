package com.vincent.forexmgt.service

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.CurrencyType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.ExchangeRate
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.IOException
import java.io.Serializable

class LoadingExchangeRateService : IntentService("LoadingExchangeRateService") {

    override fun onHandleIntent(intent: Intent?) {
        val receiver = intent?.getParcelableExtra(Constants.KEY_RECEIVER) as ResultReceiver

        val response: Connection.Response
        try {
            response = Jsoup.connect("https://www.findrate.tw/bank/8/#.XHv2PKBS8dU").execute()
        } catch (e: IOException) {
            receiver.send(0, null)
            return
        }

        val webContent = response.body()
        val tableRows = Jsoup.parse(webContent)
            .select("div[id=right]")
            .select("table>tbody>tr")

        val rowCount = tableRows.size
        tableRows.removeAt(rowCount - 1)
        tableRows.removeAt(rowCount - 2)
        tableRows.removeAt(0)

        val rates = mutableListOf<ExchangeRate>()

        for (row in tableRows) {
            val tableCells = row.select("td")
            val currencyTitle = tableCells.get(0).select("a").text()
            val currencyType = CurrencyType.fromTitle(currencyTitle)

            rates.add(
                ExchangeRate(
                    currencyType?.iconRes ?: R.drawable.flag_unknown,
                    currencyTitle,
                    tableCells.get(4).text().toDouble(),
                    tableCells.get(3).text().toDouble()
                )
            )
        }

        val bundle = Bundle()
        bundle.putSerializable(Constants.KEY_RATE, rates as Serializable)

        receiver.send(0, bundle)
    }

}