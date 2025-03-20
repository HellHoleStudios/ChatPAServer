package top.hhs.xgn

import io.ktor.server.application.*
import io.prometheus.metrics.core.datapoints.DistributionDataPoint
import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Histogram
import io.prometheus.metrics.exporter.httpserver.HTTPServer
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics

object StatisticManager {

    var tokensServedCounter: Counter? = null
    var requestCounter: Counter? = null
    var eventDuration: DistributionDataPoint? = null

    var server: HTTPServer? = null

    fun incToken(from: String) {
        tokensServedCounter?.labelValues(from)?.inc()
    }

    fun incRequest(from: String) {
        requestCounter?.labelValues(from)?.inc()
    }

    fun recordWaitTime(waitTime: Long) {
        eventDuration?.observe(waitTime/1000.0)
    }

    fun init() {

        println("Init Statistic Manager")

        JvmMetrics.builder().register() // initialize the out-of-the-box JVM metrics

        tokensServedCounter = Counter.builder()
            .name("tokens_served")
            .help("total number of tokens received from the model side by the server")
            .labelNames("from")
            .register()

        requestCounter = Counter.builder()
            .name("request_count")
            .help("total number of requests received from the users")
            .labelNames("from")
            .register()

        eventDuration = Histogram.builder()
            .name("wait_time")
            .help("wait time between a job starts and finishes")
            .unit(io.prometheus.metrics.model.snapshots.Unit.SECONDS)
            .register()
    }

    fun startServer(app: Application) {

        server = HTTPServer.builder()
            .port(app.environment.config.property("prometheus.port").getString().toInt())
            .buildAndStart()
        println("HTTPServer listening on port http://localhost:" + server?.port + "/metrics")

    }

}