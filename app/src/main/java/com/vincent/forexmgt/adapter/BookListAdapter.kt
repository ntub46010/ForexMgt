package com.vincent.forexmgt.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.CurrencyType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Book

class BookListAdapter(
    var books: List<Book>)
    : RecyclerView.Adapter<BookListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books.get(position)
        holder.bindValue(book)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @BindView(R.id.imgBookIcon) lateinit var imgBookIcon: ImageView
        @BindView(R.id.imgCurrencyType) lateinit var imgCurrencyType: ImageView
        @BindView(R.id.txtBookName) lateinit var txtBookName: TextView

        init {
            ButterKnife.bind(this, v)
        }

        fun bindValue(book: Book) {
            val currencyType = CurrencyType.fromCurrencyTitle(book.currencyType)

            imgBookIcon.setImageResource(R.drawable.icon_default_book)
            imgCurrencyType.setImageResource(currencyType?.iconRes ?: R.drawable.flag_unknown)
            txtBookName.text = book.name
        }
    }

}