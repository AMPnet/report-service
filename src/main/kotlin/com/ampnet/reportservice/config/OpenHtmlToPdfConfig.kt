package com.ampnet.reportservice.config

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenHtmlToPdfConfig {

    @Bean
    fun pdfRenderer(): PdfRendererBuilder {
        return PdfRendererBuilder().useFastMode().useSVGDrawer(BatikSVGDrawer())
    }
}
