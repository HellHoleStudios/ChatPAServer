
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}

group = "top.hhs.xgn"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

//    val isDevelopment: Boolean = true
//    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-server-freemarker")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-sessions")
    implementation("io.ktor:ktor-server-csrf")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")


    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-websockets")

    // Kotlin Serialization CSV
    implementation("de.brudaswen.kotlinx.serialization:kotlinx-serialization-csv:2.1.0")

    implementation("io.prometheus:prometheus-metrics-core:1.3.6")
    implementation("io.prometheus:prometheus-metrics-instrumentation-jvm:1.3.6")
    implementation("io.prometheus:prometheus-metrics-exporter-httpserver:1.3.6")
}
