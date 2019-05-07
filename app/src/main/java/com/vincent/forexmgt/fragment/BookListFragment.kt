package com.vincent.forexmgt.fragment

import android.content.Intent
import android.os.Bundle
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
import com.vincent.forexmgt.activity.BookHomeActivity
import com.vincent.forexmgt.adapter.BookListAdapter
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.util.BundleBuilder
import com.vincent.forexmgt.util.DialogUtils
import org.apache.commons.lang3.StringUtils
import java.lang.Exception
import java.util.*

class BookListFragment : Fragment() {

    @BindView(R.id.lstBook) lateinit var lstBook: RecyclerView
    @BindView(R.id.fabCreateBook) lateinit var fabCreateBook: FloatingActionButton
    @BindView(R.id.prgBar) lateinit var prgBar: ProgressBar

    private lateinit var dlgCreateBook: AlertDialog

    private var bookService = ForExMgtApp.bookService!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        displayContent(true)
        lstBook.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL,false)
        fabCreateBook.setOnClickListener {
            dlgCreateBook.show()
        }
        prepareCreateDialog()

        loadBooks()
    }

    private fun prepareCreateDialog() {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_create_book, null) as RelativeLayout
        val tilBookName = layout.findViewById<TextInputLayout>(R.id.tilBookName)
        val edtBookName = layout.findViewById<EditText>(R.id.edtBookName)
        val spnCurrencyType = layout.findViewById<Spinner>(R.id.spnCurrencyType)

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, CurrencyType.getTitles())
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
                        val strSelectedItem = spnCurrencyType.selectedItem.toString()
                        val currencyCode = StringUtils.split(strSelectedItem, StringUtils.SPACE)[1]

                        val book = Book(
                            edtBookName.text.toString(),
                            CurrencyType.valueOf(currencyCode),
                            StringUtils.EMPTY,
                            Date()
                        )

                        createBook(book)
                        dlgCreateBook.dismiss()
                    }
                }
        }
    }

    private fun createBook(book: Book) {
        displayContent(true)

        val callback = object : Callback<Book> {
            override fun onExecute(data: Book) {
                Toast.makeText(context, getString(R.string.create_successfully), Toast.LENGTH_SHORT).show()
                loadBooks()
            }

            override fun onError(e: Exception) {
                Toast.makeText(context, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        bookService.createBook(book, callback)
    }

    private fun loadBooks() {
        displayContent(true)

        val callback = object : Callback<List<Book>> {
            override fun onExecute(data: List<Book>) {
                displayContent(false)

                val adapter = lstBook.adapter
                if (adapter == null) {
                    initAdapter(data)
                } else {
                    (adapter as BookListAdapter).refreshData(data)
                }
            }

            override fun onError(e: Exception) {
                displayContent(false)
                Toast.makeText(context, getString(R.string.load_book_error), Toast.LENGTH_SHORT).show()
            }
        }

        bookService.loadBooks(callback)
    }

    private fun goBookHomePage(book: Book) {
        val intent = BundleBuilder()
            .putString(Constants.PROPERTY_ID, book.obtainId())
            .putString(Constants.PROPERTY_NAME, book.name)
            .appendToIntent(Intent(context, BookHomeActivity::class.java))

        startActivity(intent)
    }

    private fun displayContent(isLoading: Boolean) {
        if (isLoading) {
            lstBook.visibility = View.INVISIBLE
            prgBar.visibility = View.VISIBLE
        } else {
            lstBook.visibility = View.VISIBLE
            prgBar.visibility = View.INVISIBLE
        }
    }

    private fun initAdapter(books: List<Book>) {
        val adapter = BookListAdapter(books)

        adapter.setOnItemClickListener(object : RecyclerViewOnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val book = adapter.getItem(position)
                goBookHomePage(book)
            }
        })

        lstBook.adapter = adapter
    }
}