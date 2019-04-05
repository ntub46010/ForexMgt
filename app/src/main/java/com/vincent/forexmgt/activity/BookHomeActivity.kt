package com.vincent.forexmgt.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.firestore.ListenerRegistration
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.Operator
import com.vincent.forexmgt.R
import com.vincent.forexmgt.adapter.EntryPagerAdapter
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.fragment.EntryListFragment
import com.vincent.forexmgt.service.BookService
import com.vincent.forexmgt.service.EntryService
import com.vincent.forexmgt.util.BundleBuilder

class BookHomeActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tab) lateinit var tab: TabLayout
    @BindView(R.id.vpgEntry) lateinit var vpgEntry: ViewPager
    @BindView(R.id.fabCreateEntry) lateinit var fabCreateEntry: FloatingActionButton

    private lateinit var bundle: Bundle
    private lateinit var book: Book

    private lateinit var bookService: BookService
    private var bookListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_home)
        bindService(Intent(this, BookService::class.java), bookServiceConn, Context.BIND_AUTO_CREATE)
        ButterKnife.bind(this)
        bundle = intent.extras

        toolbar.title = bundle.getString(Constants.PROPERTY_NAME)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        tab.setupWithViewPager(vpgEntry)
        vpgEntry.offscreenPageLimit = 2

        prepareFragments()

        fabCreateEntry.setOnClickListener {
            goEntryEditPage()
        }
    }

    private fun prepareFragments() {
        val creditFragment = BundleBuilder()
            .putString(Constants.PROPERTY_BOOK_ID, bundle.getString(Constants.PROPERTY_ID))
            .putString(Constants.KEY_ENTRY_TYPE, EntryType.CREDIT.name)
            .appendToFragment(EntryListFragment())

        val debitFragment = BundleBuilder()
            .putString(Constants.PROPERTY_BOOK_ID, bundle.getString(Constants.PROPERTY_ID))
            .putString(Constants.KEY_ENTRY_TYPE, EntryType.DEBIT.name)
            .appendToFragment(EntryListFragment())

        val balanceFragment = BundleBuilder()
            .putString(Constants.PROPERTY_BOOK_ID, bundle.getString(Constants.PROPERTY_ID))
            .putString(Constants.KEY_ENTRY_TYPE, EntryType.BALANCE.name)
            .appendToFragment(EntryListFragment())

        val adapter = EntryPagerAdapter(supportFragmentManager)
        adapter.addFragment(getString(R.string.tab_credit), creditFragment)
        adapter.addFragment(getString(R.string.tab_debit), debitFragment)
        adapter.addFragment(getString(R.string.tab_balance), balanceFragment)
        vpgEntry.adapter = adapter
    }

    private fun goEntryEditPage() {
        val bundleBuilder = BundleBuilder()

        when(vpgEntry.currentItem) {
            0 -> {
                val intent = bundleBuilder
                    .putString(Constants.KEY_TITLE, getString(R.string.title_create_credit_entry))
                    .putString(Constants.KEY_ENTRY_TYPE, EntryType.CREDIT.name)
                    .putSerializable(Constants.KEY_BOOK, book)
                    .appendToIntent(Intent(this, EntryEditActivity::class.java))

                startActivity(intent)
            }

            1 -> {
                val intent = bundleBuilder
                    .putString(Constants.KEY_TITLE, getString(R.string.title_create_debit_entry))
                    .putString(Constants.KEY_ENTRY_TYPE, EntryType.DEBIT.name)
                    .putSerializable(Constants.KEY_BOOK, book)
                    .appendToIntent(Intent(this, EntryEditActivity::class.java))

                startActivity(intent)
            }
        }
    }

    private fun loadBook(bookId: String) {
        val operator = object : Operator {
            override fun execute(result: Any?) {
                if (result == null) {
                    Toast.makeText(this@BookHomeActivity, getString(R.string.load_book_error), Toast.LENGTH_SHORT).show()
                } else {
                    book = result as Book
                    toolbar.title = book.name
                }

                fabCreateEntry.visibility = View.VISIBLE
            }
        }

        bookListener = bookService.subscribeBook(bookId, operator)
    }

    override fun onDestroy() {
        super.onDestroy()
        bookListener?.remove()
    }

    private val bookServiceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bookService = (service as BookService.CollectionBinder).getService()
            loadBook(bundle.getString(Constants.PROPERTY_ID))
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }
}
