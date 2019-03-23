package com.vincent.forexmgt

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.adapter.EntryPagerAdapter
import com.vincent.forexmgt.fragment.TempFragment

class BookHomeActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tab) lateinit var tab: TabLayout
    @BindView(R.id.vpgEntry) lateinit var vpgEntry: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_home)
        ButterKnife.bind(this)
        val bundle = intent.extras

        toolbar.title = bundle.getString(Constants.KEY_NAME)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        tab.setupWithViewPager(vpgEntry)

        Toast.makeText(this, bundle.getString(Constants.KEY_ID), Toast.LENGTH_SHORT).show()
        prepareFragments()
    }

    private fun prepareFragments() {
        val adapter = EntryPagerAdapter(supportFragmentManager)
        adapter.addFragment("轉入", TempFragment())
        adapter.addFragment("轉出", TempFragment())
        adapter.addFragment("結餘", TempFragment())
        vpgEntry.adapter = adapter
    }
}
