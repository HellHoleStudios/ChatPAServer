package top.hhs.xgn

import io.ktor.server.websocket.*
import io.ktor.websocket.*

/**
 * A class to store the session of a request used in [ModelSocketManager]
 *
 */
class RequestSession(var session: DefaultWebSocketServerSession) {
    suspend fun close(closeReason: CloseReason) {
        session.close(closeReason)
    }

    suspend fun send(message: Frame.Text) {
        session.send(message)
    }

    enum class RequestStatus {
        NO_QUESTION,
        WAITING,
        RUNNING,
        LOST,
        ENDED
    }

    var lastQuestion = ""
    var answer = ""
    var status = RequestStatus.NO_QUESTION
    var startTime: Long = -1
    var endTime: Long = -1
}