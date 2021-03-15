package com.ampnet.reportservice.controller

import com.ampnet.core.jwt.UserPrincipal
import com.ampnet.core.jwt.exception.TokenException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder

internal object ControllerUtils {
    fun getUserPrincipalFromSecurityContext(): UserPrincipal =
        SecurityContextHolder.getContext().authentication.principal as? UserPrincipal
            ?: throw TokenException("SecurityContext authentication principal must be UserPrincipal")

    fun getHttpHeadersForPdf(): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_PDF
        return httpHeaders
    }
}
