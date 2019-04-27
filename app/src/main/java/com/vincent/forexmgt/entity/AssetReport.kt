package com.vincent.forexmgt.entity

import java.io.Serializable

data class AssetReport(
    val generalAssetSummaries: List<GeneralAssetSummary>,
    val subAssetSummariesGroup: List<List<SubAssetSummary>>)
    : Serializable