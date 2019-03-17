package com.vincent.forexmgt.fragment

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vincent.forexmgt.Constants
import com.vincent.forexmgt.CurrencyType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.adapter.BookListAdapter
import com.vincent.forexmgt.entity.Book
import com.vincent.forexmgt.util.DialogUtils
import org.apache.commons.lang3.StringUtils
import java.util.*

class BookFragment : Fragment() {

    @BindView(R.id.lstBook) lateinit var lstBook: RecyclerView
    @BindView(R.id.fabCreateBook) lateinit var fabCreateBook: FloatingActionButton

    private lateinit var dlgCreateBook: AlertDialog

    private lateinit var db: FirebaseFirestore
    private lateinit var currentLoginUser: FirebaseUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_book_home, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        db = FirebaseFirestore.getInstance()
        currentLoginUser = FirebaseAuth.getInstance().currentUser!!

        lstBook.layoutManager = GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL,false)

        fabCreateBook.setOnClickListener {
            dlgCreateBook.show()
        }

        prepareCreateDialog()

        loadBooks()
    }

    private fun prepareCreateDialog() {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_create_book, null) as RelativeLayout
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
            .setPositiveButton(getString(R.string.ok)) { dialogInterface, i ->
                createBook(Book(
                    edtBookName.text.toString(),
                    spnCurrencyType.selectedItem.toString(),
                    StringUtils.EMPTY,
                    Date()
                ))

                edtBookName.text = null
                spnCurrencyType.setSelection(0)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }

    private fun createBook(book: Book) {
        if (StringUtils.isEmpty(book.name)) {
            Toast.makeText(context, "未輸入帳簿名稱", Toast.LENGTH_SHORT).show()
            return
        }

        if (StringUtils.isEmpty(book.creator)) {
            book.creator = currentLoginUser.uid
        }

        db.collection(Constants.COLLECTION_BOOK)
            .add(book)
            .addOnSuccessListener {
                Toast.makeText(context, getString(R.string.create_successfully), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "${getString(R.string.create_failed)}\n${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBooks() {
        db.collection(Constants.COLLECTION_BOOK)
            .whereEqualTo(Constants.PROPERTY_CREATOR, currentLoginUser.uid)
            .orderBy(Constants.PROPERTY_CREATED_TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                val books = querySnapshot?.toObjects(Book::class.java)?.toList()!!

                val adapter = lstBook.adapter
                if (adapter == null) {
                    lstBook.adapter = BookListAdapter(books)
                } else {
                    (adapter as BookListAdapter).books = books
                    adapter.notifyDataSetChanged()
                }
            }
    }
}