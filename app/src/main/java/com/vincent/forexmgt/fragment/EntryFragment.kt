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
import com.vincent.forexmgt.adapter.EntryCreditListAdapter
import com.vincent.forexmgt.adapter.EntryDebitListAdapter
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
            35.1234
        )

        val entry2 = EntryCredit(
            "id2",
            "bookId",
            Date(1),
            "EUR",
            2000.0,
            60000,
            35.2345
        )

        val entry3 = EntryCredit(
            "id3",
            "bookId",
            Date(3),
            "EUR",
            1000.0,
            30000,
            35.3456
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryCreditListAdapter(context!!, entries)
    }

    private fun displayDebitList() {
        val entry1 = EntryDebit(
            "id1",
            "bookId",
            Date(1),
            "USD",
            3000.0,
            91000,
            90000,
            30.1234
        )

        val entry2 = EntryDebit(
            "id2",
            "bookId",
            Date(1),
            "USD",
            2000.0,
            61000,
            60000,
            30.2345
        )

        val entry3 = EntryDebit(
            "id3",
            "bookId",
            Date(3),
            "USD",
            1000.0,
            31000,
            30000,
            30.3456
        )

        val entries = mutableListOf(entry1, entry2, entry3)

        lstEntry.adapter = EntryDebitListAdapter(context!!, entries)
    }

    private fun displayBalanceList() {

    }

}