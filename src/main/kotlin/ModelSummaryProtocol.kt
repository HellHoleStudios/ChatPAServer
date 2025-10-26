package top.hhs.xgn

import kotlinx.serialization.Serializable


/**
 * DTO for a summary request to the model side. Please do NOT modify the variable names.
 *
 * **The `summary` field MUST be set to TRUE for compatibility!!!**
 *
 * @see ModelReturnProtocol
 * @see ModelSendProtocol
 */
@Serializable
data class ModelSummaryProtocol(val summary:Boolean, val token: String, val history: List<UserHistory>)
