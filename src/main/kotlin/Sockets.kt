package top.hhs.xgn

import freemarker.cache.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    routing {
        authenticate("token") {
            webSocket("/") { // websocketSession
                val session=call.sessions.get<UserSession>()!!
                val token=session.token!!
                var lastQuery=session.lastQuery

                val username = TokenManager.tokens[token]!!

                ModelSocketManager.registerSession(token,this)

                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()

                        log.debug("Received: $text from $username with token $token")

                        //TODO this might not work when user refresh, fix it later
                        val newTime = System.currentTimeMillis()
                        if(newTime-lastQuery<1000){
                            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"Query too fast"))
                            return@webSocket
                        }
                        lastQuery=newTime

                        if(text=="$$"){
                            //consider this as a summary request
                            val ans=ModelSocketManager.sendSummaryRequest(token)
                            if(!ans){
                                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY,"禁止双开(╯‵□′)╯︵┻━┻"))
                            }
                        }else {
                            val ans = ModelSocketManager.sendRequest(
                                ModelSendProtocol(
                                    text,
                                    token,
                                    username,
                                    HistoryManager.getUserHistory(token).toMutableList()
                                )
                            )
                            if (!ans) {
                                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "禁止双开(╯‵□′)╯︵┻━┻"))
                            }
                        }

                        //Test code below
//                        delay(2000)
//                        outgoing.send(Frame.Text("S"))
//                        for(i in 0..100){
//                            outgoing.send(Frame.Text("T"+ text.random()))
//                            delay(50)
//                        }
//                        outgoing.send(Frame.Text("E"))
                    }
                }
            }
        }

    }
}
