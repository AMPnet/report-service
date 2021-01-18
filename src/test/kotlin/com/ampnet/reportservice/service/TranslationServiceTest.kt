package com.ampnet.reportservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TranslationServiceTest : JpaServiceTestBase() {

    @Test
    fun mustGenerateTranslationsInEnglishOnInvalidLanguage() {
        verify("Transaction service fallback to english on invalid language") {
            val translations = translationService.getTranslations("invalid")
            assertThat(translations.transactions).isEqualTo("Transactions")
        }
    }
}
