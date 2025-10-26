package top.hhs.xgn

import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    GlobalScope.launch {
        ModelSocketManager.initApplication(this@module)
        while(true){
            try{
                if(ModelSocketManager.receiving){
                    delay(10000)
                    continue
                }
                ModelSocketManager.initConnection()

                GlobalScope.launch {
                    ModelSocketManager.receiveLoop()
                }
            }catch(e:Exception){
                log.error("Could not connect to model server: $e")
            }

            delay(10000)
        }
    }

    configureSecurity()
    configureSockets()
    configureTemplating()
    configureRouting()
}
