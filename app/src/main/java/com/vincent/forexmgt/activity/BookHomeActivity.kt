package com.vincent.forexmgt.activity

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.firestore.ListenerRegistration
import com.vincent.forexmgt.*
import com.vincent.forexmgt.adapter.EntryPagerAdapter
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.entity.Entry
import com.vincent.forexmgt.entity.ExchangeRate
import com.vincent.forexmgt.fragment.EntryListFragment
import com.vincent.forexmgt.service.LoadingExchangeRateService
import com.vincent.forexmgt.util.BundleBuilder
import com.vincent.forexmgt.util.DialogUtils

class BookHomeActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tab) lateinit var tab: TabLayout
    @BindView(R.id.vpgEntry) lateinit var vpgEntry: ViewPager
    @BindView(R.id.fabCreateEntry) lateinit var fabCreateEntry: FloatingActionButton

    private lateinit var bundle: Bundle
    private lateinit var book: Book

    private lateinit var dlgWaiting: Dialog

    private var bookListener: ListenerRegistration? = null

    private var bookService = ForExMgtApp.bookService
    private var entryService = ForExMgtApp.entryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_home)
        ButterKnife.bind(this)
        bundle = intent.extras
        dlgWaiting = DialogUtils.getWaitingDialog(this)

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

        subscribeBook(bundle.getString(Constants.PROPERTY_ID))
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

            2 -> {
                // 外幣餘額 * 即期匯率 = 臺幣現值
                // 臺幣現值 - 臺幣成本 = 利差
                createBalanceEntry()
            }
        }
    }

    private fun createBalanceEntry() {
        val receiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                dlgWaiting.dismiss()

                if (resultData == null) {
                    Toast.makeText(this@BookHomeActivity, "沒有網路連線", Toast.LENGTH_SHORT).show()
                    return
                }

                val rates = resultData.getSerializable(Constants.KEY_RATE) as List<ExchangeRate>
                val entry = entryService.generateBalanceEntry(book, rates)
                showBalanceEntryInfo(entry)
            }
        }

        val intent = Intent(this, LoadingExchangeRateService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        startService(intent)

        dlgWaiting.show()
    }

    private fun showBalanceEntryInfo(entry: Entry) {
        val operator = object : Operator {
            override fun execute(result: Any?) {

            }
        }

        //entryService.createEntry(entry, operator)
    }

    private fun subscribeBook(bookId: String) {
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

}
