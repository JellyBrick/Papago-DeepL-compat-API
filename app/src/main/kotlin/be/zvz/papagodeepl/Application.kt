package be.zvz.papagodeepl

import be.zvz.papagodeepl.plugins.Http.configureHTTP
import be.zvz.papagodeepl.plugins.Routing.configureRouting
import be.zvz.papagodeepl.plugins.Serialization.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit =
    EngineMain.main(args)

@Suppress("unused") // application.yaml references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureRouting()
}
