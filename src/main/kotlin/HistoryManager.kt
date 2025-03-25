package top.hhs.xgn

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File

object HistoryManager {

    val cache = HashMap<String, ArrayList<UserHistory>>()

    /**
     * Get the user history from the cache or the disk.
     */
    fun getUserHistory(token: String): ArrayList<UserHistory> {
        if (token in cache) {
            return cache[token]!!
        }

        //read from file
        val file = File("history/$token.json")
        if(!file.exists()){
            cache[token] = ArrayList()
            return cache[token]!!
        }
        cache[token] = Json.decodeFromString<ArrayList<UserHistory>>(file.readText())
        return cache[token]!!
    }

    /**
     * Append a history to the user's history.
     */
    fun appendHistory(token: String, message: UserHistory) {
        val his = getUserHistory(token)
        his.add(message)
        if (his.size > USER_HISTORY_SIZE_THRESHOLD) {
            his.removeAt(0)
        }

        //save history to disk
        val file=File("history/$token.json")
        file.writeText(Json.encodeToString(his))
    }

    /**
     * Clear the user's history both in cache and disk.
     */
    fun clearHistory(token: String) {
        cache.remove(token)
        val file=File("history/$token.json")
        file.delete()
    }

    //TODO: config this
    const val USER_HISTORY_SIZE_THRESHOLD = 10

}