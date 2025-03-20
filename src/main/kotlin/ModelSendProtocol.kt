package top.hhs.xgn

import kotlinx.serialization.Serializable

@Serializable
data class ModelSendProtocol(val prompt:String, val token:String,val user: String, val history: ArrayList<UserHistory>)
