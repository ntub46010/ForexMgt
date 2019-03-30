package com.vincent.forexmgt.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.R

class EntryEditActivity : AppCompatActivity() {

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_edit)
        ButterKnife.bind(this)
        val bundle = intent.extras

        toolbar.title = bundle.getString(Constants.KEY_ENTRY_TYPE) ?: "Default Title"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
