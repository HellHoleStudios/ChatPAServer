package top.hhs.xgn

import ch.qos.logback.core.model.Model
import freemarker.cache.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import java.io.File

lateinit var adminToken:String

fun RoutingContext.getObjectMap(): MutableMap<String, Any?> {
    return getObjectMapWithSession(call.sessions.get<UserSession>())
}

fun Application.getConfig(name: String):String = this.environment.config.property(name).getString()
fun RoutingCall.getToken():String? = getSession()?.token
fun RoutingCall.getSession() = this.sessions.get<UserSession>()

fun getObjectMapWithSession(session: UserSession?):MutableMap<String,Any?>{
    return if(session==null){
        mutableMapOf("session" to UserSession(), "name" to null, "admin" to false)
    }else{
        mutableMapOf("session" to session, "name" to TokenManager.tokens[session.token], "admin" to (session.token==adminToken))
    }
}

fun Application.configureRouting() {
    adminToken=this.getConfig("token.admin")

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if(cause is NoShowException){
                call.respond(FreeMarkerContent("error.ftl", getObjectMapWithSession(call.sessions.get<UserSession>()).also { it["error"]=cause.message }))
                return@exception
            }
            call.respond(FreeMarkerContent("error.ftl", getObjectMapWithSession(call.sessions.get<UserSession>()).also { it["error"]="Internal Server Error: $cause" }))
        }
    }

    val self=this

    routing {

        authenticate("token") {
            get("/") {
                if(self.getConfig("ktor.development").toBoolean()){
                    TokenManager.reloadTokens(self)
                    ModelSocketManager.logger=self.log
                }

                call.respond(FreeMarkerContent("index.ftl", getObjectMap()))
            }

            post("/clearHistory"){
                HistoryManager.clearHistory(call.getToken()!!)
                call.respond("OK")
            }
        }


        authenticate("admin") {
            get("/gc"){
                System.gc()
                call.respond("GCed")
            }
            get("/rs"){
                ModelSocketManager.sessionRegisters.clear()
                call.respond("RSed")
            }

            get("/session"){
                call.respond(FreeMarkerContent("session.ftl",getObjectMap().also {
                    it["sessions"]=ModelSocketManager.sessionRegisters.entries.toList()
                    it["currentTime"]=System.currentTimeMillis()
                }))
            }
            get("/token"){
                //token management page
                call.respond(FreeMarkerContent("token.ftl",getObjectMap().also {
                    it["token_count"]=TokenManager.tokens.size
                    it["tokens"]=TokenManager.tokens.keys.toString()
                }))
            }
            get("/tokenFile"){
                call.respondFile(File("tokens.csv"))
            }
            post("/tokenUpload"){
                val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 2)

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if(part.originalFileName==null || part.originalFileName?.length==0){
                                throw NoShowException("请上传文件")
                            }

                            val file = File("tokens.csv")
                            file.copyTo(File("_tokens.csv"), overwrite = true)

                            part.provider().copyAndClose(file.writeChannel())

                            TokenManager.reloadTokens(self)
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                call.respondRedirect("/token")
            }

            get("/inits"){
                log.info("Init statistics server...")
                StatisticManager.init(self)
                StatisticManager.startServer(self)

                call.respond("OK")
            }

            get("/init"){

                if(ModelSocketManager.receiving){
                    throw NoShowException("连接已经打开，无须再次启动")
                }

                log.info("Init Model Sockets...")
                ModelSocketManager.initApplication(self)
                ModelSocketManager.initConnection()

                GlobalScope.launch {
                    ModelSocketManager.receiveLoop()
                }

                call.respond("OK")
            }
        }

        get("/login"){

            if(self.getConfig("ktor.development").toBoolean()){
                TokenManager.reloadTokens(self)
            }

            call.respond(FreeMarkerContent("login.ftl",getObjectMap()))
        }

        post("/login"){
            val post = call.receiveParameters()
            val token = post["token"]

            if(TokenManager.tokens.containsKey(token)){
                call.sessions.set(UserSession(token,System.currentTimeMillis()))
                call.respondRedirect("/")
            }else{
                throw NoShowException("Token无效")
            }
        }

        get("/logout"){
            call.sessions.set(UserSession())
            call.respondRedirect("/")
        }

        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
