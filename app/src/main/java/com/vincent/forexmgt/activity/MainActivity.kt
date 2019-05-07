package com.vincent.forexmgt.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.vincent.forexmgt.ForExMgtApp
import com.vincent.forexmgt.R
import com.vincent.forexmgt.fragment.AssetReportFragment
import com.vincent.forexmgt.fragment.BookListFragment
import com.vincent.forexmgt.fragment.ExchangeRateFragment

class MainActivity : AppCompatActivity() {
    // http://givemepass.blogspot.com/2015/11/recylerviewcardview.html
    // http://givemepass.blogspot.com/2015/11/title.html
    // https://android.devdon.com/archives/149
    // https://www.jianshu.com/p/47ffaac11e06

    @BindView(R.id.navBar) lateinit var navBar: BottomNavigationView

    private lateinit var fragmentManager: FragmentManager
    private var currentFragment: Fragment? = null
    private var exchangeRateFragment: ExchangeRateFragment? = null
    private var bookListFragment: BookListFragment? = null
    private var assetReportFragment: AssetReportFragment? = null

    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        prepareAuthentication()
        fragmentManager = supportFragmentManager

        setupNavigationBar()

        exchangeRateFragment = ExchangeRateFragment()
        switchContent(exchangeRateFragment!!)

        FirebaseFirestore.getInstance().firestoreSettings =
            FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == this.RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                val response = IdpResponse.fromResultIntent(data)
                Toast.makeText(this, response?.error?.errorCode.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as ForExMgtApp).unbindService()
    }

    private fun prepareAuthentication() {
        val authProvider = listOf(
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val authListener =
            FirebaseAuth.AuthStateListener {
                val firebaseUser = it.currentUser
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
                    ForExMgtApp.currentLoginUser = firebaseUser
                    ForExMgtApp.bookService?.setLoginUser(firebaseUser)
                }
            }

        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    private fun setupNavigationBar() {
        navBar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navExchangeRateList -> {
                    if (exchangeRateFragment == null) {
                        exchangeRateFragment = ExchangeRateFragment()
                    }
                    switchContent(exchangeRateFragment!!)
                }
                R.id.navBook -> {
                    if (bookListFragment == null) {
                        bookListFragment = BookListFragment()
                    }
                    switchContent(bookListFragment!!)
                }
                R.id.navSummary -> {
                    if (assetReportFragment == null) {
                        assetReportFragment = AssetReportFragment()
                    }
                    switchContent(assetReportFragment!!)
                }
            }

            true
        }

        navBar.setOnNavigationItemReselectedListener {

        }
    }

    private fun switchContent(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()

        if (currentFragment == null) {
            transaction.add(R.id.frameLayout, fragment)
        } else {
            if (fragment.isAdded) {
                transaction.hide(currentFragment).show(fragment)
            } else {
                transaction.hide(currentFragment).add(R.id.frameLayout, fragment)
            }
        }

        transaction.commit()
        currentFragment = fragment
    }

    @OnClick(R.id.btnSignOut)
    fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, getString(R.string.sign_out_successfully), Toast.LENGTH_SHORT).show()
            }
    }

}
