package com.vincent.forexmgt.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import butterknife.BindView
import butterknife.ButterKnife
import com.vincent.forexmgt.CurrencyType
import com.vincent.forexmgt.R
import com.vincent.forexmgt.adapter.AssetSummaryAdapter
import com.vincent.forexmgt.entity.SubAssetSummary
import com.vincent.forexmgt.entity.GeneralAssetSummary

class AssetSummaryFragment : Fragment() {

    @BindView(R.id.listView) lateinit var listView: ExpandableListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_asset_summary, container, false)
        ButterKnife.bind(this, view)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sub1 = SubAssetSummary(
            "美金帳簿1",
            1000.0,
            31000,
            30000
        )

        val sub2 = SubAssetSummary(
            "美金帳簿2",
            2000.0,
            62000,
            60000
        )

        val gen1 = GeneralAssetSummary(
            CurrencyType.USD,
            3000.0,
            93000,
            30.0
        )

        val sub3 = SubAssetSummary(
            "歐元帳簿1",
            100.0,
            3600,
            3500
        )

        val sub4 = SubAssetSummary(
            "歐元帳簿2",
            300.0,
            10800,
            10500
        )

        val gen2 = GeneralAssetSummary(
            CurrencyType.EUR,
            400.0,
            14400,
            35.0
        )

        val gens = listOf(gen1, gen2)

        val subs = listOf(
            listOf(sub1, sub2),
            listOf(sub3, sub4)
        )

        listView.setAdapter(AssetSummaryAdapter(context!!, gens, subs))
    }
}