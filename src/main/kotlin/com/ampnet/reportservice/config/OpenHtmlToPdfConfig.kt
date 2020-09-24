package com.ampnet.reportservice.config

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.slf4j.Slf4jLogger
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import com.openhtmltopdf.util.XRLog
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class OpenHtmlToPdfConfig {

    @Bean
    fun pdfRenderer(): PdfRendererBuilder {
        return PdfRendererBuilder().useFastMode().useSVGDrawer(BatikSVGDrawer())
    }

    @PostConstruct
    fun setLogging() {
        XRLog.setLoggerImpl(Slf4jLogger())
    }
}
