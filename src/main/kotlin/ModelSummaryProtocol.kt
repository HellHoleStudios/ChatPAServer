package top.hhs.xgn

import kotlinx.serialization.Serializable

@Serializable
data class ModelSummaryProtocol(val summary:Boolean, val token: String, val history: ArrayList<UserHistory>)
