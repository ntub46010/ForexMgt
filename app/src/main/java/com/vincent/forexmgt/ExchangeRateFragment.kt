package com.vincent.forexmgt

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife

class ExchangeRateFragment : Fragment() {

    @BindView(R.id.textView) lateinit var textView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_exchange_rate, container, false)
        ButterKnife.bind(this, view)

        textView.text = "AAA"

        return view
    }
}