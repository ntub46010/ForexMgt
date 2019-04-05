package com.vincent.forexmgt.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.firestore.ListenerRegistration
import com.vincent.forexmgt.*
import com.vincent.forexmgt.adapter.EntryListAdapter
import com.vincent.forexmgt.entity.*

class EntryListFragment : Fragment() {

    @BindView(R.id.lstEntry) lateinit var lstEntry: ListView

    private var entryService = ForExMgtApp.entryService!!
    private var entryListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_entry, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bookId = arguments?.getString(Constants.PROPERTY_BOOK_ID)!!
        val entryType = EntryType.fromName(arguments?.getString(Constants.KEY_ENTRY_TYPE))!!
        subscribeEntries(bookId, entryType)
    }

    private fun subscribeEntries(bookId: String, entryType: EntryType) {
        val operator = object : Operator {
            override fun execute(result: Any?) {
                if (result is Exception) {
                    Toast.makeText(context, "${getString(R.string.load_entry_error)}\n${result.message}", Toast.LENGTH_SHORT).show()
                    return
                }

                val entries = result as List<Entry>
                val adapter = lstEntry.adapter

                if (adapter == null) {
                    val entryAdapter = EntryListAdapter(context!!, entries)
                    lstEntry.adapter = entryAdapter
                } else {
                    (adapter as EntryListAdapter).entries = entries
                    adapter.notifyDataSetChanged()
                }
            }
        }

        entryListener = entryService.subscribeEntries(bookId, entryType, operator)
    }

    override fun onDestroy() {
        super.onDestroy()
        entryListener?.remove()
    }

}