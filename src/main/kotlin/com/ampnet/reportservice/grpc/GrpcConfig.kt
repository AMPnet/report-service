package com.ampnet.reportservice.grpc

import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfig {

    @Bean
    fun authenticationReader(): GrpcAuthenticationReader {
        return BasicGrpcAuthenticationReader()
    }

    @Bean
    fun globalInterceptorConfigurerAdapter(): GlobalClientInterceptorConfigurer {
        return GlobalClientInterceptorConfigurer { registry ->
            registry.add(GrpcLogInterceptor())
        }
    }
}
