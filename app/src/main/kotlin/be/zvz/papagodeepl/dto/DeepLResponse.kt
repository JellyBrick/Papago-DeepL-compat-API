package be.zvz.papagodeepl.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DeepLResponse(
    val translations: List<TranslationResult>,
) {
    data class TranslationResult(
        @JsonProperty("detected_source_language")
        val detectedSourceLanguage: String,
        val text: String,
    )
}
