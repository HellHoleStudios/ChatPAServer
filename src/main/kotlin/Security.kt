package top.hhs.xgn

import freemarker.cache.*
import io.ktor.http.*
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
import io.ktor.util.*
import io.ktor.websocket.*
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable
import java.io.File

fun Application.configureSecurity() {
    install(Sessions) {
        //TODO: config this
        val secretEncryptKey = hex("1145141919810acceedd00ffacdefdde")
        val secretSignKey = hex("99824435319260817fffffeeabc91723")
        cookie<UserSession>("user_session", directorySessionStorage(File(".sessions"), cached = true)) {
            cookie.extensions["SameSite"] = "lax"
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }

    install(Authentication){
        session<UserSession>("token"){
            challenge {
                call.respondRedirect("/login")
            }
            validate { session ->
                return@validate session.token
            }
        }
        session<UserSession>("admin"){
            challenge {
                call.respondRedirect("/login")
            }
            validate { session ->
                if(session.token!=application.getConfig("token.admin")){
                    return@validate null
                }

                return@validate session.token
            }
        }
    }

    //TODO: CSRF
}
