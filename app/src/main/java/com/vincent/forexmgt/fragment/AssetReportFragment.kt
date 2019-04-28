package com.vincent.forexmgt.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ProgressBar
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.*
import com.vincent.forexmgt.adapter.AssetSummaryAdapter
import com.vincent.forexmgt.entity.*
import com.vincent.forexmgt.service.LoadingExchangeRateService
import com.vincent.forexmgt.service.PrepareAssetReportService
import java.io.Serializable

class AssetReportFragment : Fragment() {

    @BindView(R.id.listView) lateinit var listView: ExpandableListView
    @BindView(R.id.refreshLayout) lateinit var refreshLayout: SwipeRefreshLayout
    @BindView(R.id.prgBar) lateinit var prgBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_asset_summary, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        prgBar.visibility = View.VISIBLE
        refreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        refreshLayout.setOnRefreshListener { startPrepareReport() }

        startPrepareReport()
    }

    private fun startPrepareReport() {
        val receiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultData == null) {
                    refreshLayout.isRefreshing = false
                    prgBar.visibility = View.INVISIBLE
                    Toast.makeText(context, getString(R.string.load_exchange_rate_error), Toast.LENGTH_SHORT).show()
                    return
                }

                val rates = resultData.getSerializable(Constants.KEY_RATE) as List<ExchangeRate>
                prepareAssetReport(rates)
            }
        }

        val intent = Intent(context, LoadingExchangeRateService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        context?.startService(intent)
    }

    private fun prepareAssetReport(rates: List<ExchangeRate>) {
        val receiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                refreshLayout.isRefreshing = false
                prgBar.visibility = View.INVISIBLE

                val assetReport = resultData?.getSerializable(Constants.KEY_REPORT) as AssetReport
                var adapter = listView.expandableListAdapter

                if (adapter == null) {
                    adapter = AssetSummaryAdapter(context!!,
                        assetReport.currencyReports, assetReport.currencyToBookReportsMap)
                    listView.setAdapter(adapter)
                } else {
                    (adapter as AssetSummaryAdapter)
                        .refreshData(assetReport.currencyReports, assetReport.currencyToBookReportsMap)
                }
            }
        }

        val intent = Intent(context, PrepareAssetReportService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        intent.putExtra(Constants.KEY_RATE, rates as Serializable)
        context?.startService(intent)
    }

}