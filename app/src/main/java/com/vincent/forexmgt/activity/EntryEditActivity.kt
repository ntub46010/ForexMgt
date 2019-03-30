package com.vincent.forexmgt.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Book

class EntryEditActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    private lateinit var book: Book
    private lateinit var entryType: EntryType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_edit)
        ButterKnife.bind(this)
        val bundle = intent.extras

        toolbar.title = bundle.getString(Constants.KEY_TITLE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        book = bundle.getSerializable(Constants.KEY_BOOK) as Book
        entryType = EntryType.fromName(bundle.getString(Constants.KEY_ENTRY_TYPE))!!
    }
}
