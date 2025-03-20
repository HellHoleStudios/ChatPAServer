package top.hhs.xgn

import kotlinx.serialization.Serializable

@Serializable
data class UserHistory(val role: String, val content: String, val time: Long) {
}