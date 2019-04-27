package com.vincent.forexmgt.entity

data class AssetReport(
    val generalAssetSummaries: List<GeneralAssetSummary>,
    val subAssetSummariesGroup: List<List<SubAssetSummary>>)