package com.vincent.forexmgt.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Book
import org.apache.commons.lang3.StringUtils

class EntryEditActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tilDate) lateinit var tilDate: TextInputLayout
    @BindView(R.id.tilFcyAmt) lateinit var tilFcyAmt: TextInputLayout
    @BindView(R.id.tilTwdAmt) lateinit var tilTwdAmt: TextInputLayout
    @BindView(R.id.edtDate) lateinit var edtDate: EditText
    @BindView(R.id.edtFcyAmt) lateinit var edtFcyAmt: EditText
    @BindView(R.id.edtTwdAmt) lateinit var edtTwdAmt: EditText
    @BindView(R.id.chkAddToCost) lateinit var chkAddToCost: CheckBox

    private lateinit var book: Book
    private lateinit var entryType: EntryType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_edit)
        ButterKnife.bind(this)
        val bundle = intent.extras

        book = bundle.getSerializable(Constants.KEY_BOOK) as Book

        toolbar.title = bundle.getString(Constants.KEY_TITLE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        entryType = EntryType.fromName(bundle.getString(Constants.KEY_ENTRY_TYPE))!!
        if (entryType == EntryType.CREDIT) {
            chkAddToCost.visibility = View.VISIBLE
            tilFcyAmt.hint = "${getString(R.string.fcy_amt_credit)}（${book.currencyType?.title}）"
            tilTwdAmt.hint = getString(R.string.twd_amt_debit)
        } else if (entryType == EntryType.DEBIT) {
            chkAddToCost.visibility = View.GONE
            tilFcyAmt.hint = "${getString(R.string.fcy_amt_debit)}（${book.currencyType?.title}）"
            tilTwdAmt.hint = getString(R.string.twd_amt_credit)
        }

    }

    @OnClick(R.id.btnSubmit)
    fun createEntry() {
        validateData()
        if (!StringUtils.isAllEmpty(tilDate.error, tilFcyAmt.error, tilTwdAmt.error)) {
            return
        }

        Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show()

    }

    private fun validateData() {
        tilDate.error = null
        tilFcyAmt.error = null
        tilTwdAmt.error = null

        val date = edtDate.text.toString()
        val fcyAmt = edtFcyAmt.text.toString()
        val twdAmt = edtTwdAmt.text.toString()

        if (StringUtils.isEmpty(date)) {
            tilDate.error = getString(R.string.mandatory_field)
        }

        if (StringUtils.isEmpty(fcyAmt)) {
            tilFcyAmt.error = getString(R.string.mandatory_field)
        }

        if (StringUtils.isEmpty(twdAmt)) {
            tilTwdAmt.error = getString(R.string.mandatory_field)
        }

    }

    @OnClick(R.id.btnBack)
    fun back() {
        finish()
    }
}
