package com.vincent.forexmgt.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.*
import com.vincent.forexmgt.adapter.ExchangeRateAdapter
import com.vincent.forexmgt.entity.ExchangeRate
import com.vincent.forexmgt.service.LoadingExchangeRateService

class ExchangeRateFragment : Fragment() {

    @BindView(R.id.lstExchangeRate) lateinit var lstExchangeRate: RecyclerView
    @BindView(R.id.refreshLayout) lateinit var refreshLayout: SwipeRefreshLayout
    @BindView(R.id.prgBar) lateinit var prgBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_exchange_rate_home, container, false)
        ButterKnife.bind(this, view)

        prgBar.visibility = View.VISIBLE
        lstExchangeRate.layoutManager = LinearLayoutManager(context)
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        refreshLayout.setOnRefreshListener { loadExchangeRate() }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadExchangeRate()
    }

    private fun loadExchangeRate() {
        val receiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                refreshLayout.isRefreshing = false
                prgBar.visibility = View.INVISIBLE

                if (resultData == null) {
                    Toast.makeText(activity, getString(R.string.load_exchange_rate_error), Toast.LENGTH_SHORT).show()
                    return
                }

                val rates = resultData.getSerializable(Constants.KEY_RATE) as List<ExchangeRate>
                val adapter = lstExchangeRate.adapter

                if (adapter == null) {
                    lstExchangeRate.adapter = ExchangeRateAdapter(rates)
                } else {
                    (adapter as ExchangeRateAdapter).exchangeRates = rates
                    adapter.notifyDataSetChanged()
                }
            }
        }

        val intent = Intent(activity, LoadingExchangeRateService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        activity?.startService(intent)
    }
}