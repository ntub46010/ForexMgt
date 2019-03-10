package com.vincent.forexmgt

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.jsoup.Jsoup

class MainActivity : AppCompatActivity() {
    // http://givemepass.blogspot.com/2015/11/recylerviewcardview.html

    @BindView(R.id.lst_exchange_rate) lateinit var lstExchangeRate: RecyclerView

    private var firebaseUser: FirebaseUser? = null

    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        prepareAuth()

        lstExchangeRate.layoutManager = LinearLayoutManager(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == this.RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                val response = IdpResponse.fromResultIntent(data)
                Toast.makeText(applicationContext, response?.error?.errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareAuth() {
        val authProvider = listOf(
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val authListener =
            FirebaseAuth.AuthStateListener { auth ->
                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    val intent = AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(authProvider)
                            .setLogo(R.drawable.logo)
                            .setTheme(R.style.LoginTheme)
                            .setAlwaysShowSignInMethodScreen(true)
                            .setIsSmartLockEnabled(false)
                            .build()
                    startActivityForResult(intent, this.RC_SIGN_IN)
                } else {
                    this.firebaseUser = firebaseUser
                    displayExchangeRate()
                }
            }

        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    private fun displayExchangeRate() {
        object : Thread() {
            override fun run() {
                super.run()
                val response = Jsoup.connect("https://www.findrate.tw/bank/8/#.XHv2PKBS8dU").execute()
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
                    val currencyType = CurrencyType.fromCurrencyTitle(currencyTitle)

                    rates.add(
                        ExchangeRate(
                            currencyType?.iconRes ?: R.drawable.flag_usd,
                            currencyTitle,
                            tableCells.get(4).text().toDouble(),
                            tableCells.get(3).text().toDouble())
                    )
                }

                runOnUiThread {
                    lstExchangeRate.adapter = ExchangeRateAdapter(rates)
                }
            }
        }.start()
    }

    @OnClick(R.id.btnSignOut)
    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "您已登出", Toast.LENGTH_SHORT).show()
            }
    }

}
