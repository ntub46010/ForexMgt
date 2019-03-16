package com.vincent.forexmgt.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.R
import com.vincent.forexmgt.adapter.BookListAdapter
import com.vincent.forexmgt.entity.Book

class BookFragment : Fragment() {

    @BindView(R.id.lstBook) lateinit var lstBook: RecyclerView
    @BindView(R.id.fabCreateBook) lateinit var fabCreateBook: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_book_home, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val books = listOf(
            Book("帳簿一", "日幣 JPY"),
            Book("帳簿二", "歐元 EUR"),
            Book("帳簿三", "港幣 HKD"),
            Book("帳簿四", "澳幣 AUD"),
            Book("帳簿五", "南非幣 ZAR")
        )

        lstBook.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL,false)
        lstBook.adapter = BookListAdapter(books)

        fabCreateBook.setOnClickListener {
            Toast.makeText(context, "AAA", Toast.LENGTH_SHORT).show()
        }
    }
}