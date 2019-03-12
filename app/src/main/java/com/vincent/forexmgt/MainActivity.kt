package com.vincent.forexmgt

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.design.widget.BottomNavigationView
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    // http://givemepass.blogspot.com/2015/11/recylerviewcardview.html
    // http://givemepass.blogspot.com/2015/11/title.html
    // https://android.devdon.com/archives/149

//    @BindView(R.id.lstExchangeRate) lateinit var lstExchangeRate: RecyclerView
//    @BindView(R.id.refreshLayout) lateinit var refreshLayout: SwipeRefreshLayout
//    @BindView(R.id.prgBar) lateinit var prgBar: ProgressBar
    @BindView(R.id.navBar) lateinit var navBar: BottomNavigationView

    private var firebaseUser: FirebaseUser? = null

    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        prepareAuth()

//        prgBar.visibility = View.VISIBLE
//        lstExchangeRate.layoutManager = LinearLayoutManager(this)
//        refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
//        refreshLayout.setOnRefreshListener { loadExchangeRate() }

        setupNavigationBar()
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
                    loadExchangeRate()
                }
            }

        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    private fun setupNavigationBar() {
        navBar.setOnNavigationItemSelectedListener { item ->
            val transaction = supportFragmentManager.beginTransaction()

            when(item.itemId) {
                R.id.navHome -> {
                    transaction.replace(R.id.frameLayout, ExchangeRateFragment())
                }
                R.id.navBook -> {

                }
                R.id.navThird -> {

                }
            }

            transaction.addToBackStack(null)
            transaction.commit()
            true
        }

    }

    private fun loadExchangeRate() {
//        val receiver = object : ResultReceiver(Handler()) {
//            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
//                refreshLayout.isRefreshing = false
//                prgBar.visibility = View.INVISIBLE
//
//                if (resultData == null) {
//                    Toast.makeText(this@MainActivity, "沒有網路連線", Toast.LENGTH_SHORT).show()
//                    return
//                }
//
//                val rates = resultData.getSerializable(Constants.KEY_RATE) as List<ExchangeRate>
//                val adapter = lstExchangeRate.adapter
//
//                if (adapter == null) {
//                    lstExchangeRate.adapter = ExchangeRateAdapter(rates)
//                } else {
//                    (adapter as ExchangeRateAdapter).exchangeRates = rates
//                    adapter.notifyDataSetChanged()
//                }
//            }
//        }
//
//        val intent = Intent(this, LoadingExchangeRateService::class.java)
//        intent.putExtra(Constants.KEY_RECEIVER, receiver)
//        startService(intent)
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
