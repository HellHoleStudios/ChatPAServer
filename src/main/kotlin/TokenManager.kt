package top.hhs.xgn

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.decodeFromString
import java.io.File

object TokenManager {

    @Serializable
    data class CsvRow(val Name: String, val Token: String)

    val tokens = HashMap<String,String>()

    @OptIn(ExperimentalSerializationApi::class)
    fun reloadTokens(){
        val csvFile = File("tokens.csv")

        // Create a Csv instance
        val csv = Csv {
            hasHeaderRecord = true // Indicates the CSV has a header row
        }

        // Read the CSV file
        val rows: List<CsvRow> = csv.decodeFromString(csvFile.readText().replace("\r\n","\n"))

        for(i in rows){
            tokens[i.Token]=i.Name

            println("Loaded token for ${i.Name}: ${i.Token}")
        }
    }
}