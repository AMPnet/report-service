package com.ampnet.reportservice

import com.ampnet.reportservice.config.JsonConfig
import com.ampnet.reportservice.service.data.Translations
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("test")
@Import(JsonConfig::class)
@ExtendWith(SpringExtension::class)
abstract class TestBase {

    protected val userLanguage = "EN"

    private val allTranslations by lazy {
        getTranslationsMap()
    }

    protected fun suppose(@Suppress("UNUSED_PARAMETER") description: String, function: () -> Unit) {
        function.invoke()
    }

    protected fun verify(@Suppress("UNUSED_PARAMETER") description: String, function: () -> Unit) {
        function.invoke()
    }

    @Autowired
    @Qualifier("camelCaseObjectMapper")
    protected lateinit var camelCaseObjectMapper: ObjectMapper

    protected fun getTranslations(userLanguage: String): Translations {
        val translations = mutableMapOf<String, String>()
        allTranslations.forEach { (key, map) ->
            val translation = map.getOrElse(userLanguage, { map["en"] })!!
            translations[key] = translation
        }
        return Translations(translations)
    }

    private fun getTranslationsMap(): Map<String, Map<String, String>> {
        val json = javaClass.classLoader.getResource("templates/translations.json")?.readText()!!
        return camelCaseObjectMapper.readValue(json)
    }
}
