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
import com.vincent.forexmgt.activity.BookHomeActivity
import com.vincent.forexmgt.adapter.AssetReportAdapter
import com.vincent.forexmgt.entity.*
import com.vincent.forexmgt.service.LoadingExchangeRateService
import com.vincent.forexmgt.service.PrepareAssetReportService
import com.vincent.forexmgt.util.BundleBuilder
import java.io.Serializable

class AssetReportFragment : Fragment() {

    @BindView(R.id.listView) lateinit var listView: ExpandableListView
    @BindView(R.id.refreshLayout) lateinit var refreshLayout: SwipeRefreshLayout
    @BindView(R.id.prgBar) lateinit var prgBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_asset_report, container, false)
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
                val data = resultData?.getSerializable(Constants.KEY_DATA)
                if (data is Exception) {
                    refreshLayout.isRefreshing = false
                    prgBar.visibility = View.INVISIBLE
                    Toast.makeText(context, "${getString(R.string.load_exchange_rate_error)}\n${data.message}", Toast.LENGTH_SHORT).show()
                    return
                }

                val rates = data as List<ExchangeRate>
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

                val data = resultData?.getSerializable(Constants.KEY_DATA)
                if (data is Exception) {
                    Toast.makeText(activity, "${getString(R.string.generate_asset_report_error)}\n${data.message}", Toast.LENGTH_SHORT).show()
                    return
                }

                val assetReport = data as AssetReport
                val adapter = listView.expandableListAdapter

                if (adapter == null) {
                    initAdapter(assetReport)
                } else {
                    (adapter as AssetReportAdapter)
                        .refreshData(assetReport.currencyReports, assetReport.currencyToBookReportsMap)
                }
            }
        }

        val intent = Intent(context, PrepareAssetReportService::class.java)
        intent.putExtra(Constants.KEY_RECEIVER, receiver)
        intent.putExtra(Constants.KEY_RATE, rates as Serializable)
        context?.startService(intent)
    }

    private fun initAdapter(assetReport: AssetReport) {
        val adapter = AssetReportAdapter(context!!,
            assetReport.currencyReports, assetReport.currencyToBookReportsMap)

        listView.setAdapter(adapter)

        listView.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            val bookReport = adapter.getChild(groupPosition, childPosition) as BookAssetReport

            val intent = BundleBuilder()
                .putString(Constants.PROPERTY_ID, bookReport.bookId)
                .putString(Constants.PROPERTY_NAME, bookReport.bookName)
                .appendToIntent(Intent(context, BookHomeActivity::class.java))
            startActivity(intent)

            true
        }
    }

}