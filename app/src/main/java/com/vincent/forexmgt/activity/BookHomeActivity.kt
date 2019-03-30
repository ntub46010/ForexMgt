package com.vincent.forexmgt.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.adapter.EntryPagerAdapter
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.fragment.EntryListFragment
import com.vincent.forexmgt.util.BundleUtils

class BookHomeActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tab) lateinit var tab: TabLayout
    @BindView(R.id.vpgEntry) lateinit var vpgEntry: ViewPager
    @BindView(R.id.fabCreateEntry) lateinit var fabCreateEntry: FloatingActionButton

    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_home)
        ButterKnife.bind(this)
        book = intent.extras.getSerializable(Constants.KEY_BOOK) as Book

        toolbar.title = book.name
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        tab.setupWithViewPager(vpgEntry)

        prepareFragments()

        fabCreateEntry.setOnClickListener {
            goEntryEditPage()
        }

    }

    private fun goEntryEditPage() {
        when(vpgEntry.currentItem) {
            0 -> {
                val intent = Intent(this, EntryEditActivity::class.java)
                val bundle = BundleUtils.getBundle(Constants.KEY_BOOK, book)
                bundle.putString(Constants.KEY_ENTRY_TYPE, EntryType.CREDIT.name)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            1 -> {
                val intent = Intent(this, EntryEditActivity::class.java)
                val bundle = BundleUtils.getBundle(Constants.KEY_BOOK, book)
                bundle.putString(Constants.KEY_ENTRY_TYPE, EntryType.DEBIT.name)
                intent.putExtras(bundle)
                startActivity(intent)
            }

            2 -> {

            }
        }
    }

    private fun prepareFragments() {
        val creditFragment = EntryListFragment()
        creditFragment.arguments = BundleUtils.getBundle(Constants.KEY_ENTRY_TYPE, EntryType.CREDIT.name)

        val debitFragment = EntryListFragment()
        debitFragment.arguments = BundleUtils.getBundle(Constants.KEY_ENTRY_TYPE, EntryType.DEBIT.name)

        val balanceFragment = EntryListFragment()
        balanceFragment.arguments = BundleUtils.getBundle(Constants.KEY_ENTRY_TYPE, EntryType.BALANCE.name)

        val adapter = EntryPagerAdapter(supportFragmentManager)
        adapter.addFragment(getString(R.string.tab_credit), creditFragment)
        adapter.addFragment(getString(R.string.tab_debit), debitFragment)
        adapter.addFragment(getString(R.string.tab_balance), balanceFragment)
        vpgEntry.adapter = adapter
    }
}
