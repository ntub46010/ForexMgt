package com.vincent.forexmgt.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.RecyclerViewOnItemClickListener
import com.vincent.forexmgt.R
import com.vincent.forexmgt.entity.Book

class BookListAdapter(
    private var books: List<Book>)
    : RecyclerView.Adapter<BookListAdapter.ViewHolder>(), View.OnClickListener {
    // http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0327/2647.html

    private var onItemClickListener: RecyclerViewOnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        view.setOnClickListener(this)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return books.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.itemView.tag = position
        holder.bindValue(book)
    }

    fun getItem(position: Int): Book {
        return books[position]
    }

    fun setOnItemClickListener(listener: RecyclerViewOnItemClickListener?) {
        onItemClickListener = listener
    }

    fun refreshData(books: List<Book>) {
        this.books = books
        notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        if (onItemClickListener != null) {
            onItemClickListener?.onItemClick(v, v?.getTag() as Int)
        }
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @BindView(R.id.imgBookIcon) lateinit var imgBookIcon: ImageView
        @BindView(R.id.imgCurrencyType) lateinit var imgCurrencyType: ImageView
        @BindView(R.id.txtBookName) lateinit var txtBookName: TextView

        init {
            ButterKnife.bind(this, v)
        }

        fun bindValue(book: Book) {
            imgBookIcon.setImageResource(R.drawable.icon_default_book)
            imgCurrencyType.setImageResource(book.currencyType?.iconRes ?: R.drawable.flag_unknown)
            txtBookName.text = book.name
        }
    }

}