package top.hhs.xgn

import io.ktor.server.application.*

/**
 * This code will NOT be called!!! Read the Ktor documentation for more information.
 */
fun main(args: Array<String>) {
//    TokenManager.reloadTokens()

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    TokenManager.reloadTokens(this)
    ModelSocketManager.logger = this.log

    configureSecurity()
    configureSockets()
    configureTemplating()
    configureRouting()
}
