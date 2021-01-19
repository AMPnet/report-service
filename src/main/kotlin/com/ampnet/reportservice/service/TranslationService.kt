package com.ampnet.reportservice.service

import com.ampnet.reportservice.service.data.Translations

interface TranslationService {
    fun getTranslations(language: String): Translations
}
