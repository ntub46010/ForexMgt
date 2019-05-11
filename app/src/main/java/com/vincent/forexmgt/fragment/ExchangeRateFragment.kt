package com.vincent.forexmgt.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import org.apache.commons.lang3.StringUtils

class ExchangeRateFragment : Fragment() {

    @BindView(R.id.txtBankName) lateinit var txtBankName: TextView
    @BindView(R.id.lstExchangeRate) lateinit var lstExchangeRate: RecyclerView
    @BindView(R.id.refreshLayout) lateinit var refreshLayout: SwipeRefreshLayout
    @BindView(R.id.prgBar) lateinit var prgBar: ProgressBar

    private lateinit var dlgChooseBank: AlertDialog

    private lateinit var selectedBank: Bank
    private lateinit var sp: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_exchange_rate, container, false)
        ButterKnife.bind(this, view)

        lstExchangeRate.layoutManager = LinearLayoutManager(context)
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        refreshLayout.setOnRefreshListener { loadExchangeRate(selectedBank) }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sp = context!!.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

        val defBankEng = sp.getString(Constants.KEY_DEFAULT_BROWSE_BANK, Bank.FUBON.name)
        selectedBank = Bank.valueOf(defBankEng)
        txtBankName.text = selectedBank.chineseName

        val bankNames = Bank.getChineseTitles()
        dlgChooseBank = AlertDialog.Builder(context!!)
            .setTitle(getString(R.string.choose_bank))
            .setItems(Bank.getChineseTitles().toTypedArray()) { dialogInterface, i ->
                val bankChineseName = bankNames[i]
                if (StringUtils.equals(bankChineseName, selectedBank.chineseName)) {
                    sp.edit()
                        .putString(Constants.KEY_DEFAULT_BROWSE_BANK, selectedBank.name)
                        .apply()
                    Toast.makeText(context, getString(R.string.default_browsing_rate, selectedBank.chineseName), Toast.LENGTH_SHORT).show()
                    return@setItems
                }

                selectedBank = Bank.fromChineseName(bankChineseName)!!
                prgBar.visibility = View.VISIBLE
                lstExchangeRate.visibility = View.INVISIBLE
                loadExchangeRate(selectedBank)
            }
            .create()

        prgBar.visibility = View.VISIBLE
        lstExchangeRate.visibility = View.INVISIBLE
        loadExchangeRate(selectedBank)
    }

    private fun loadExchangeRate(bank: Bank) {
        txtBankName.text = bank.chineseName

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