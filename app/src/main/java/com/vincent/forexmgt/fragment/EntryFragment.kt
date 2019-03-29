package com.vincent.forexmgt.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.EntryType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.adapter.EntryListAdapter
import com.vincent.forexmgt.entity.*
import java.util.*

class EntryFragment : Fragment() {

    @BindView(R.id.lstEntry) lateinit var lstEntry: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_entry, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val entryType = EntryType.fromName(arguments?.getString(Constants.KEY_TYPE))

        when (entryType) {
            EntryType.CREDIT -> displayCreditList()
            EntryType.DEBIT -> displayDebitList()
            EntryType.BALANCE -> displayBalanceList()
        }
    }

    private fun displayCreditList() {
        val entry1 = Entry(
            "id1",
            "bookId",
            Date(1),
            EntryType.CREDIT,
            "USD",
            3000.0,
            90000,
        30.0,
            90000,
            0,
            0
        )

        val entry2 = Entry(
            "id2",
            "bookId",
            Date(1),
            EntryType.CREDIT,
            "USD",
            2000.0,
            60000,
            30.0,
            60000,
            0,
            0
        )

        val entry3 = Entry(
            "id3",
            "bookId",
            Date(1),
            EntryType.CREDIT,
            "USD",
            1000.0,
            30000,
            30.0,
            30000,
            0,
            0
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryListAdapter(context!!, entries)
    }

    private fun displayDebitList() {
        val entry1 = Entry(
            "id4",
            "bookId",
            Date(1),
            EntryType.DEBIT,
            "EUR",
            3000.0,
            91000,
            35.1234,
            0,
            90000,
            0
        )

        val entry2 = Entry(
            "id5",
            "bookId",
            Date(1),
            EntryType.DEBIT,
            "EUR",
            2000.0,
            61000,
            35.2345,
            0,
            60000,
            0
        )

        val entry3 = Entry(
            "id6",
            "bookId",
            Date(1),
            EntryType.DEBIT,
            "EUR",
            1000.0,
            31000,
            35.3456,
            0,
            30000,
            0
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryListAdapter(context!!, entries)
    }

    private fun displayBalanceList() {
        val entry1 = Entry(
            "id7",
            "bookId",
            Date(1),
            EntryType.BALANCE,
            "SEK",
            3000.333,
            9000,
            3.1234,
            0,
            0,
            450
        )

        val entry2 = Entry(
            "id8",
            "bookId",
            Date(1),
            EntryType.BALANCE,
            "SEK",
            2000.22,
            6000,
            3.2345,
            0,
            0,
            300
        )

        val entry3 = Entry(
            "id9",
            "bookId",
            Date(1),
            EntryType.BALANCE,
            "SEK",
            1000.1,
            3000,
            3.3456,
            0,
            0,
            -150
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryListAdapter(context!!, entries)
    }

}