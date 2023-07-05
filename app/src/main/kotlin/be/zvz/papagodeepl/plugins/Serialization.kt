package be.zvz.papagodeepl.plugins

import com.fasterxml.jackson.module.blackbird.BlackbirdModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

object Serialization {
    val blackbirdModule = BlackbirdModule()
    fun Application.configureSerialization() {
        install(ContentNegotiation) {
            jackson() {
                registerKotlinModule()
                registerModule(blackbirdModule)
            }
        }
    }
}
