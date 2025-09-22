package top.hhs.xgn

import io.ktor.server.application.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object TokenManager {

    @Serializable
    data class CsvRow(val Name: String, val Token: String)

    val tokens = ConcurrentHashMap<String, String>()

    @OptIn(ExperimentalSerializationApi::class)
    fun reloadTokens(app: Application){

        app.log.info("Loading tokens.csv...")

        val csvFile = File("tokens.csv")

        app.log.debug("Raw content: ${csvFile.readText()}")

        // Create a Csv instance
        val csv = Csv {
            hasHeaderRecord = true // Indicates the CSV has a header row
        }

        // Read the CSV file
        val rows: List<CsvRow> = csv.decodeFromString(csvFile.readText().replace("\r\n","\n"))

        for(i in rows){
            tokens[i.Token]=i.Name

            app.log.info("Loaded token for ${i.Name}: ${i.Token}")
        }
    }
}