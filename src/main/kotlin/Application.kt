package top.hhs.xgn

import io.ktor.server.application.*

fun main(args: Array<String>) {
    TokenManager.reloadTokens()
//    StatisticManager.init()

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    //setup websocket first
//    ModelSocketManager.initApplication(this)
//    ModelSocketManager.initConnection()
    TokenManager.reloadTokens()

    configureSecurity()
    configureSockets()
    configureTemplating()
    configureSerialization()
    configureRouting()
}
