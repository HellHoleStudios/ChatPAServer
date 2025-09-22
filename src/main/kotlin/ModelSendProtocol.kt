package top.hhs.xgn

import kotlinx.serialization.Serializable

/**
 * DTO for the sending data to the model side. Please do NOT modify the variable names.
 *
 * @see ModelSummaryProtocol
 * @see ModelReturnProtocol
 */
@Serializable
data class ModelSendProtocol(val prompt:String, val token:String,val user: String, val history: MutableList<UserHistory>)
