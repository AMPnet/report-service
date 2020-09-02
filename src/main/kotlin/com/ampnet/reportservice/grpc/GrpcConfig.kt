package com.ampnet.reportservice.grpc

import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfig {

    @Bean
    fun globalInterceptorConfigurerAdapter(): GlobalClientInterceptorConfigurer {
        return GlobalClientInterceptorConfigurer { registry ->
            registry.add(GrpcLogInterceptor())
        }
    }
}
