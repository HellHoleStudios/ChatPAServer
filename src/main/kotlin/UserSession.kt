package top.hhs.xgn

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(var token: String? = null, var lastQuery: Long = 0)
