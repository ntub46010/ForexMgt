package com.vincent.forexmgt.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.*
import com.vincent.forexmgt.adapter.BookListAdapter
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.service.BookService
import com.vincent.forexmgt.util.DialogUtils
import org.apache.commons.lang3.StringUtils
import java.util.*

class BookFragment : Fragment() {

    @BindView(R.id.lstBook) lateinit var lstBook: RecyclerView
    @BindView(R.id.fabCreateBook) lateinit var fabCreateBook: FloatingActionButton

    private lateinit var dlgCreateBook: AlertDialog

    private lateinit var bookService: BookService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_book_home, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        context?.bindService(Intent(activity, BookService::class.java), bookServiceConn, Context.BIND_AUTO_CREATE)

        lstBook.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL,false)

        fabCreateBook.setOnClickListener {
            dlgCreateBook.show()
        }

        prepareCreateDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unbindService(bookServiceConn)
    }

    private fun prepareCreateDialog() {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_create_book, null) as RelativeLayout
        val tilBookName = layout.findViewById<TextInputLayout>(R.id.tilBookName)
        val edtBookName = layout.findViewById<EditText>(R.id.edtBookName)
        val spnCurrencyType = layout.findViewById<Spinner>(R.id.spnCurrencyType)

        val currencyTitles = mutableListOf<String>()
        for (type in CurrencyType.values()) {
            currencyTitles.add(type.title)
        }

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, currencyTitles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCurrencyType.adapter = adapter

        dlgCreateBook = DialogUtils.getPlainDialog(context!!, getString(R.string.title_create_book), getString(R.string.desc_create_book))
            .setView(layout)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dlgCreateBook.setOnShowListener {
            tilBookName.error = null
            edtBookName.text = null
            spnCurrencyType.setSelection(0)

            dlgCreateBook.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (StringUtils.isEmpty(edtBookName.text)) {
                        tilBookName.error = getString(R.string.mandatory_field)
                    } else {
                        createBook(Book(
                            StringUtils.EMPTY,
                            edtBookName.text.toString(),
                            spnCurrencyType.selectedItem.toString(),
                            StringUtils.EMPTY,
                            Date()
                        ))

                        dlgCreateBook.dismiss()
                    }
                }
        }
    }

    private fun createBook(book: Book) {
        bookService.createBook(book)
    }

    private fun loadBooks() {
        val operator = object : Operator {
            override fun execute(result: Any?) {
                val books = result as List<Book>
                var adapter = lstBook.adapter

                if (adapter == null) {
                    adapter = BookListAdapter(books)
                    adapter.setOnItemClickListener(object : RecyclerViewOnItemClickListener {
                        override fun onItemClick(view: View?, position: Int) {
                            goBookHomePage(books[position])
                        }
                    })
                    lstBook.adapter = adapter
                } else {
                    (adapter as BookListAdapter).books = books
                    adapter.notifyDataSetChanged()
                }
            }
        }
        bookService.loadBooks(operator)
    }

    fun goBookHomePage(book: Book) {
        val intent = Intent(context, BookHomeActivity::class.java)
        val bundle = Bundle()
        bundle.putString(Constants.KEY_ID, book.id)
        bundle.putString(Constants.KEY_NAME, book.name)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    private val bookServiceConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bookService = (service as BookService.CollectionBinder).getService()
            loadBooks()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }
}