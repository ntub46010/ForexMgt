package com.vincent.forexmgt.activity

import android.app.DatePickerDialog
import android.app.Dialog
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
import com.vincent.forexmgt.*
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.entity.Entry
import com.vincent.forexmgt.util.DialogUtils
import org.apache.commons.lang3.StringUtils
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class EntryEditActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.tilDate) lateinit var tilDate: TextInputLayout
    @BindView(R.id.tilFcyAmt) lateinit var tilFcyAmt: TextInputLayout
    @BindView(R.id.tilTwdAmt) lateinit var tilTwdAmt: TextInputLayout
    @BindView(R.id.edtDate) lateinit var edtDate: EditText
    @BindView(R.id.edtFcyAmt) lateinit var edtFcyAmt: EditText
    @BindView(R.id.edtTwdAmt) lateinit var edtTwdAmt: EditText
    @BindView(R.id.chkAddToCost) lateinit var chkAddToCost: CheckBox

    private lateinit var dlgWaiting: Dialog

    private lateinit var book: Book
    private lateinit var entryType: EntryType

    private var entryService = ForExMgtApp.entryService

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
            tilFcyAmt.hint = "${getString(R.string.fcy_amt_credit)}（${book.currencyType?.chineseName}）"
            tilTwdAmt.hint = getString(R.string.twd_amt_debit)
        } else if (entryType == EntryType.DEBIT) {
            chkAddToCost.visibility = View.GONE
            tilFcyAmt.hint = "${getString(R.string.fcy_amt_debit)}（${book.currencyType?.chineseName}）"
            tilTwdAmt.hint = getString(R.string.twd_amt_credit)
        }

        dlgWaiting = DialogUtils.getWaitingDialog(this)
    }

    @OnClick(R.id.btnSubmit)
    fun createEntry() {
        validateData()
        if (!StringUtils.isAllEmpty(tilDate.error, tilFcyAmt.error, tilTwdAmt.error)) {
            return
        }

        val date = edtDate.text.toString()
        val fcyAmt = edtFcyAmt.text.toString().toDouble()
        val twdAmt = edtTwdAmt.text.toString().toInt()

        val entry = Entry(
            StringUtils.EMPTY,
            book.id,
            SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN).parse(date),
            entryType,
            book.currencyType!!.name,
            fcyAmt,
            twdAmt,
            Math.round(twdAmt * 10000 / fcyAmt) / 10000.0
        )

        if (entryType == EntryType.CREDIT) {
            if (chkAddToCost.isChecked) {
                entry.twdCost = entry.twdAmt
            } else {
                entry.twdCost = 0
            }
        }

        if (entryType == EntryType.DEBIT) {
            book.fcyTotalAmt -= entry.fcyAmt
        }

        val operator = object : Operator {
            override fun execute(result: Any?) {
                if (result != null) {
                    val e = result as Exception
                    Toast.makeText(this@EntryEditActivity, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_LONG).show()
                    return
                }

                edtDate.text = null
                edtFcyAmt.text = null
                edtTwdAmt.text = null
                chkAddToCost.isChecked = true
                dlgWaiting.dismiss()
                Toast.makeText(this@EntryEditActivity, getString(R.string.create_successfully), Toast.LENGTH_SHORT).show()
            }
        }

        dlgWaiting.show()
        entryService.createEntry(entry, operator)
    }

    @OnClick(R.id.btnBack)
    fun back() {
        finish()
    }

    @OnClick(R.id.edtDate)
    fun selectDate() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, datePickListener,
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    private val datePickListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        edtDate.setText("$year/${month + 1}/$dayOfMonth")
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

        if (StringUtils.isEmpty(fcyAmt) || StringUtils.equals(fcyAmt, "0")) {
            tilFcyAmt.error = getString(R.string.mandatory_field)
        } else if (entryType == EntryType.DEBIT && fcyAmt.toDouble() > book.fcyTotalAmt) {
            tilFcyAmt.error = getString(R.string.insufficient_fcy_amt)
        }

        if (StringUtils.isEmpty(twdAmt) || StringUtils.equals(twdAmt, "0")) {
            tilTwdAmt.error = getString(R.string.mandatory_field)
        }
    }

}
