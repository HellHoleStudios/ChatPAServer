package top.hhs.xgn

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

object ModelSocketManager {

    lateinit var modelHost: String
    lateinit var modelPort: String

    lateinit var client: HttpClient
    lateinit var session: DefaultClientWebSocketSession

    val sessionRegisters = HashMap<String, RequestSession>()

    var receiving = false

    fun initApplication(application: Application) {
        modelHost = application.environment.config.property("model.host").getString()
        modelPort = application.environment.config.property("model.port").getString()

        client = HttpClient(CIO) {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }

    }

    suspend fun initConnection() {
        session = client.webSocketSession(host = modelHost, port = modelPort.toInt())
    }

    /**
     * Saves a new user session associated with a token
     *
     * Also send initial data (chat history, partial response and last summary )to the user
     */
    suspend fun registerSession(token: String, session: DefaultWebSocketServerSession) {

        if (token !in sessionRegisters) {
            sessionRegisters[token] = RequestSession(session)

            println("Registered session for token '$token' with session $session")
        } else {
            try {
                val s= sessionRegisters[token]!!
                s.close(CloseReason(CloseReason.Codes.NORMAL, "新的连接已经建立"))
                s.session = session

                //send history
                HistoryManager.getUserHistory(token).forEach {
                    session.send(Frame.Text("H" + Json.encodeToString(it)))
                }

                //send unfinished question
                if(s.status!=RequestSession.RequestStatus.ENDED && s.status!=RequestSession.RequestStatus.NO_QUESTION) {
                    session.send(Frame.Text("l" + s.lastQuestion))
                    session.send(Frame.Text("L" + s.answer))
                }

                //send last summary (unused)
                if(s.lastSummary!=""){
                    session.send(Frame.Text("s"+s.lastSummary))
                }
                println("Refreshed session for token '$token' with session $session")
            } catch (_: Exception) {

            }
        }
    }

    private fun buildSourceDocumentMarkdown(s: ArrayList<String>):String{
        return """
            > [!TIP]
            > **参考文献**
            >
            > ${s.joinToString()}
        """.trimIndent()
    }

    suspend fun receiveLoop() {
        try {
            receiving = true
            println("Keep the loop rolling!!!")
            while (true) {
                val ret = session.receiveDeserialized<ModelReturnProtocol>()
                println("Received message: $ret to '${ret.token}' session= ${sessionRegisters[ret.token]}")

                val s = sessionRegisters[ret.token]

                if(s==null){
                    println("Session is null?")
                    continue
                }

                try {
                    if (ret.type == "GEN_STARTED") {
                        s.status = RequestSession.RequestStatus.RUNNING
                        s.answer =""

                        s.send(Frame.Text("S"))
                    } else if (ret.type == "NEW_TOKEN") {
                        s.answer += ret.generated_token
                        StatisticManager.incToken(ret.token)

                        s.send(Frame.Text("T" + ret.generated_token))
                    } else if (ret.type == "GEN_FINISHED") {
                        s.status = RequestSession.RequestStatus.ENDED
                        s.endTime = System.currentTimeMillis()
                        StatisticManager.recordWaitTime(s.endTime - s.startTime)

                        //a hack to send the source documents
                        val sourceDoc = "\n" + buildSourceDocumentMarkdown(ret.source)
                        s.answer += sourceDoc
                        s.send(Frame.Text("T$sourceDoc"))

                        //push to history
                        HistoryManager.appendHistory(
                            ret.token,
                            UserHistory("bot", s.answer, System.currentTimeMillis())
                        )

                        s.send(Frame.Text("E"))
                    }else if(ret.type=="SUMMARY"){
                        s.summaryLock=false
                        s.lastSummary=ret.content
                        s.send(Frame.Text("s"+ret.content))
                    } else {
                        throw RuntimeException("Unknown message type from model host: ${ret.type}")
                    }
                } catch (e: Exception) {
                    if(s.status!=RequestSession.RequestStatus.ENDED){
                        s.status = RequestSession.RequestStatus.LOST
                    }
                    println("Cannot send message to client: $e. Well, screw that, nobody cares ┑(￣Д ￣)┍")
                }
            }
        } catch (e: Exception) {
            receiving = false
            println("Cannot keep it rolling anymore TAT!!!")
            e.printStackTrace()
        } finally {
            receiving = false
            println("Somehow terminated?!?!?")
        }
    }

    /**
     * Use new request instead
     */
    private suspend fun sendSerialized(message: ModelSendProtocol) {
        println("Sending message: $message")
        session.sendSerialized(message)
    }

    /**
     * Use new request instead
     */
    private suspend fun sendSerialized(message: ModelSummaryProtocol) {
        println("Sending summary: $message")
        session.sendSerialized(message)
    }

    /**
     * Send a summary request to the model side
     *
     * @return whether the request is accepted
     */
    suspend fun sendSummaryRequest(token: String):Boolean{
        val s= sessionRegisters[token] ?: return false

        if(s.summaryLock){
            return false
        }

        s.summaryLock=true
        sendSerialized(ModelSummaryProtocol(true,token,HistoryManager.getUserHistory(token)))

        return true
    }

    /**
     * Send a question to the model side
     *
     * @return whether the request is accepted
     */
    suspend fun sendRequest(message: ModelSendProtocol):Boolean {
        val s = sessionRegisters[message.token] ?: return false

        //reject request if there is already one
        if(s.status!=RequestSession.RequestStatus.ENDED && s.status!=RequestSession.RequestStatus.NO_QUESTION){
            return false
        }

        sendSerialized(message)

        StatisticManager.incRequest(message.token)

        //update session information
        s.startTime = System.currentTimeMillis()
        s.lastQuestion = message.prompt
        s.answer = ""
        s.status = RequestSession.RequestStatus.WAITING

        //update user history
        HistoryManager.appendHistory(message.token, UserHistory("user",message.prompt,System.currentTimeMillis()))

        return true
    }

    @Deprecated("Use sendSerialized instead")
    suspend fun send(message: String) {
        for (i in 0..4) { //retry 5 times
            try {
                session.send(message)
                break
            } catch (e: Exception) {
                println("Send message failed! $e Retrying soon...")
            }

            delay(1000)
        }

        //fatal error
        throw RuntimeException("Cannot send message to model host!! This is FATAL")
    }
}