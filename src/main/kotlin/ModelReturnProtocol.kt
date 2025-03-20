package top.hhs.xgn

import kotlinx.serialization.Serializable

@Serializable
data class ModelReturnProtocol(val type: String,
                               val token: String,
                               val generated_token: String = "",
                               val content: String = "",
                               val source: ArrayList<String> = ArrayList(),)
