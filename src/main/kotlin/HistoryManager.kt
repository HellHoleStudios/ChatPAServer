package top.hhs.xgn

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object HistoryManager {

    val cache = ConcurrentHashMap<String, CopyOnWriteArrayList<UserHistory>>()

    /**
     * Get the user history from the cache or the disk.
     */
    @Synchronized
    fun getUserHistory(token: String): MutableList<UserHistory> {
        if (cache.containsKey(token)) {
            return cache[token]!!
        }

        //read from file
        val file = File("history/$token.json")
        if(!file.exists()){
            cache[token] = CopyOnWriteArrayList()
            return cache[token]!!
        }
        cache[token] = CopyOnWriteArrayList(Json.decodeFromString<ArrayList<UserHistory>>(file.readText()))
        return cache[token]!!
    }

    /**
     * Append a history to the user's history.
     */
    @Synchronized
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
    @Synchronized
    fun clearHistory(token: String) {
        cache.remove(token)
        val file=File("history/$token.json")
        file.delete()
    }

    //TODO: config this
    const val USER_HISTORY_SIZE_THRESHOLD = 1000
    const val CONTEXT_THRESHOLD = 10*2
}