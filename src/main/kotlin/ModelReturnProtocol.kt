package top.hhs.xgn

import kotlinx.serialization.Serializable

/**
 * DTO for model return. Please do NOT modify the variable names.
 *
 * @see ModelSummaryProtocol
 * @see ModelSendProtocol
 */
@Serializable
data class ModelReturnProtocol(val type: String = "SUMMARY",
                               val token: String,
                               val generated_token: String = "",
                               val content: String = "",
                               val source: ArrayList<String> = ArrayList(),)
