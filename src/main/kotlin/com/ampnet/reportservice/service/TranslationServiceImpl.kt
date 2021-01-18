package com.ampnet.reportservice.service

import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InternalException
import com.ampnet.reportservice.service.data.Translations
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class TranslationServiceImpl(
    @Qualifier("camelCaseObjectMapper") private val objectMapper: ObjectMapper
) : TranslationService {

    val allTranslations by lazy {
        getTranslationsMap()
    }

    override fun getTranslations(language: String): Translations {
        val translations = allTranslations[language] ?: allTranslations["en"]
            ?: throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not find default[en] translation"
            )
        return Translations(translations)
    }

    private fun getTranslationsMap(): Map<String, Map<String, String>> {
        val json = javaClass.classLoader.getResource("templates/translations.json")?.readText()
            ?: throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not find translations.json"
            )
        return objectMapper.readValue(json)
    }
}
