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
import com.google.firebase.auth.FirebaseUser
import com.vincent.forexmgt.R
import com.vincent.forexmgt.fragment.BookFragment
import com.vincent.forexmgt.fragment.ExchangeRateFragment
import com.vincent.forexmgt.fragment.TempFragment

class MainActivity : AppCompatActivity() {
    // http://givemepass.blogspot.com/2015/11/recylerviewcardview.html
    // http://givemepass.blogspot.com/2015/11/title.html
    // https://android.devdon.com/archives/149
    // https://www.jianshu.com/p/47ffaac11e06

    @BindView(R.id.navBar) lateinit var navBar: BottomNavigationView

    private var firebaseUser: FirebaseUser? = null

    private lateinit var manager: FragmentManager
    private var currentFragment: Fragment? = null
    private var homeFragment: ExchangeRateFragment? = null
    private var bookFragment: BookFragment? = null
    private var thirdFragment: TempFragment? = null

    private val RC_SIGN_IN = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        prepareAuthentication()
        manager = supportFragmentManager

        setupNavigationBar()

        homeFragment = ExchangeRateFragment()
        switchContent(homeFragment!!)
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

    private fun prepareAuthentication() {
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
                }
            }

        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    private fun setupNavigationBar() {
        navBar.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navHome -> {
                    if (homeFragment == null) {
                        homeFragment = ExchangeRateFragment()
                    }
                    switchContent(homeFragment!!)
                }
                R.id.navBook -> {
                    if (bookFragment == null) {
                        bookFragment = BookFragment()
                    }
                    switchContent(bookFragment!!)
                }
                R.id.navThird -> {
                    if (thirdFragment == null) {
                        thirdFragment = TempFragment()
                    }
                    switchContent(thirdFragment!!)
                }
            }
            true
        }

        navBar.setOnNavigationItemReselectedListener {

        }
    }

    private fun switchContent(fragment: Fragment) {
        val transaction = manager.beginTransaction()

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
