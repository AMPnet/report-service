package com.ampnet.reportservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "com.ampnet.reportservice")
class ApplicationProperties {
    val jwt: JwtProperties = JwtProperties()
    var grpc: GrpcProperties = GrpcProperties()
}

class JwtProperties {
    lateinit var publicKey: String
}

@Suppress("MagicNumber")
class GrpcProperties {
    var blockchainServiceTimeout: Long = 15000
    var walletServiceTimeout: Long = 10000
    var userServiceTimeout: Long = 10000
    var projectServiceTimeout: Long = 10000
}
