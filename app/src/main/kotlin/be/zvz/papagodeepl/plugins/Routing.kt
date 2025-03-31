package be.zvz.papagodeepl.plugins

import be.zvz.clova.Clova
import be.zvz.clova.Language
import be.zvz.clova.LanguageSetting
import be.zvz.papagodeepl.dto.DeepLResponse
import be.zvz.papagodeepl.plugins.Serialization.blackbirdModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okio.IOException
import java.io.File

object Routing {
    private val papago = Clova(
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(BrotliInterceptor)
            .cache(Cache(File("cache"), 50 * 1024 * 1024))
            .build(),
        objectMapper = jacksonObjectMapper().registerKotlinModule().registerModule(blackbirdModule),
    ).translation.getValue(Clova.Translation.N2MT)

    fun Application.configureRouting() {
        routing {
            route("/v2/translate") {
                post {
                    val sysKey = System.getenv("TRANSLATE_SERVER_AUTH_KEY")
                    val params = call.receiveParameters()
                    if (call.request.headers["Authorization"] == "DeepL-Auth-Key " + sysKey || params["auth_key"] == sysKey) {
                        val text = params["text"] ?: return@post
                        val targetLangObject: Language = when ((params["target_lang"] ?: return@post).lowercase()) {
                            "en", "en-us", "en-gb" -> Language.ENGLISH
                            "ko" -> Language.KOREAN
                            "ja" -> Language.JAPANESE
                            "zh", "zh-tw", "zh-cn" -> Language.SIMPLIFIED_CHINESE
                            "es" -> Language.SPANISH
                            "fr" -> Language.FRENCH
                            "de" -> Language.DUTCH
                            "ru" -> Language.RUSSIAN
                            "pt", "pt-br", "pt-pt" -> Language.PORTUGUESE
                            "it" -> Language.ITALIAN
                            "vi" -> Language.VIETNAMESE
                            "th" -> Language.THAI
                            "id" -> Language.INDONESIAN
                            "hi" -> Language.HINDI
                            else -> {
                                call.respond(
                                    HttpStatusCode.BadRequest,
                                )
                                return@post
                            }
                        }
                        try {
                            val response = papago.translate(
                                language = LanguageSetting(source = Language.AUTO, target = targetLangObject),
                                text = text,
                                agreeToUsingTextData = false,
                                enableDictionary = false,
                            )
                            if (response.message == null) {
                                call.respond(
                                    HttpStatusCode.InternalServerError,
                                )
                                return@post
                            }
                            call.respond<DeepLResponse>(
                                message = DeepLResponse(
                                    translations = listOf(
                                        element = DeepLResponse.TranslationResult(
                                            detectedSourceLanguage = response.message!!.result.sourceLanguageType.uppercase(),
                                            text = response.message!!.result.translatedText,
                                        ),
                                    ),
                                ),
                            )
                        } catch (e: IOException) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                            )
                            return@post
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                        )
                    }
                }
            }
        }
    }
}
