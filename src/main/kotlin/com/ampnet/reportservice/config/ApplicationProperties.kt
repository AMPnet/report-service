package com.ampnet.reportservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.ampnet.reportservice")
class ApplicationProperties {
    val jwt: JwtProperties = JwtProperties()
}

class JwtProperties {
    lateinit var signingKey: String
}
