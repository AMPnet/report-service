package com.ampnet.reportservice

import com.ampnet.reportservice.service.data.Translations
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
abstract class TestBase {

    protected val userLanguage = "EN"

    protected fun suppose(@Suppress("UNUSED_PARAMETER") description: String, function: () -> Unit) {
        function.invoke()
    }

    protected fun verify(@Suppress("UNUSED_PARAMETER") description: String, function: () -> Unit) {
        function.invoke()
    }

    @Autowired
    @Qualifier("camelCaseObjectMapper")
    lateinit var camelCaseObjectMapper: ObjectMapper

    protected fun getTranslations(userLanguage: String): Translations {
        val json = javaClass.classLoader.getResource("templates/translations.json")?.readText()
        val typeRef = object : TypeReference<Map<String, Map<String, String>>>() {}
        val allTranslations = camelCaseObjectMapper.readValue<Map<String, Map<String, String>>>(json, typeRef)
        val translations: HashMap<String, String> = HashMap()
        allTranslations.forEach { (key, map) ->
            map.forEach { (language, translation) ->
                if (language.equals(userLanguage, true)) translations[key] = translation
            }
        }
        return Translations.from(translations)
    }
}
