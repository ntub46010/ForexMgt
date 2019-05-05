package com.vincent.forexmgt.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.vincent.forexmgt.*
import com.vincent.forexmgt.adapter.ExchangeRateAdapter
import com.vincent.forexmgt.entity.ExchangeRate
import com.vincent.forexmgt.service.LoadingExchangeRateService

class ExchangeRateFragment : Fragment() {

    @BindView(R.id.txtBankName) lateinit var txtBankName: TextView
    @BindView(R.id.lstExchangeRate) lateinit var lstExchangeRate: RecyclerView
    @BindView(R.id.refreshLayout) lateinit var refreshLayout: SwipeRefreshLayout
    @BindView(R.id.prgBar) lateinit var prgBar: ProgressBar

    private lateinit var dlgChooseBank: AlertDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_exchange_rate, container, false)
        ButterKnife.bind(this, view)

        prgBar.visibility = View.VISIBLE
        lstExchangeRate.layoutManager = LinearLayoutManager(context)
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        refreshLayout.setOnRefreshListener {
            val bankName = txtBankName.text.toString()
            loadExchangeRate(Bank.fromChineseName(bankName)!!)
        }

        txtBankName.text = Bank.FUBON.chineseName

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bankNames = Bank.getChineseNames()
        dlgChooseBank = AlertDialog.Builder(context!!)
            .setTitle(getString(R.string.choose_bank))
            .setItems(Bank.getChineseNames().toTypedArray()) { dialogInterface, i ->
                prgBar.visibility = View.VISIBLE
                lstExchangeRate.visibility = View.INVISIBLE
                txtBankName.text = bankNames[i]

                loadExchangeRate(Bank.fromChineseName(bankNames[i])!!)
            }
            .create()

        loadExchangeRate(Bank.FUBON)
    }

    private fun loadExchangeRate(bank: Bank) {
        val receiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                refreshLayout.isRefreshing = false
                prgBar.visibility = View.INVISIBLE
                lstExchangeRate.visibility = View.VISIBLE

                val data = resultData?.getSerializable(Constants.KEY_DATA)
                if (data is Exception) {
                    Toast.makeText(context, getString(R.string.load_exchange_rate_error), Toast.LENGTH_SHORT).show()
                    return
                }

                val rates = data as List<ExchangeRate>
                val adapter = lstExchangeRate.adapter

                if (adapter == null) {
                    initAdapter(rates)
                } else {
                    (adapter as ExchangeRateAdapter).refreshData(rates)
                }
            }
        }

        val intent = Intent(context, LoadingExchangeRateService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        intent.putExtra(Constants.KEY_BANK_NAME, bank.name)
        context?.startService(intent)
    }

    private fun initAdapter(rates: List<ExchangeRate>) {
        lstExchangeRate.adapter = ExchangeRateAdapter(rates)
    }

    @OnClick(R.id.btnChooseBank)
    fun showBankDialog() {
        dlgChooseBank.show()
    }

}