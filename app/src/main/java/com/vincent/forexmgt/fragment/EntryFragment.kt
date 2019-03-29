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
import com.vincent.forexmgt.entity.EntryBalance
import com.vincent.forexmgt.entity.EntryCredit
import com.vincent.forexmgt.entity.EntryDebit
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
        val entry1 = EntryCredit(
            "id1",
            "bookId",
            Date(1),
            "EUR",
            3000.0,
            90000,
        30.0,
            90000
        )

        val entry2 = EntryCredit(
            "id2",
            "bookId",
            Date(1),
            "EUR",
            2000.0,
            60000,
            30.0,
            60000
        )

        val entry3 = EntryCredit(
            "id3",
            "bookId",
            Date(3),
            "EUR",
            1000.0,
            30000,
            30.0,
            30000
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryListAdapter(context!!, entries, EntryType.CREDIT)
    }

    private fun displayDebitList() {
        val entry1 = EntryDebit(
            "id1",
            "bookId",
            Date(1),
            "USD",
            3000.0,
            91000,
            30.1234,
            90000
        )

        val entry2 = EntryDebit(
            "id2",
            "bookId",
            Date(1),
            "USD",
            2000.0,
            61000,
            30.2345,
            60000
        )

        val entry3 = EntryDebit(
            "id3",
            "bookId",
            Date(3),
            "USD",
            1000.0,
            31000,
            30.3456,
            30000
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryListAdapter(context!!, entries, EntryType.DEBIT)
    }

    private fun displayBalanceList() {
        val entry1 = EntryBalance(
            "id1",
            "bookId",
            Date(1),
            "SEK",
            3000.333,
            9000,
            3.1234,
            450
        )

        val entry2 = EntryBalance(
            "id2",
            "bookId",
            Date(1),
            "SEK",
            2000.22,
            6000,
            3.2345,
            300
        )

        val entry3 = EntryBalance(
            "id3",
            "bookId",
            Date(3),
            "SEK",
            1000.1,
            3000,
            3.3456,
            -150
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryListAdapter(context!!, entries, EntryType.BALANCE)
    }

}