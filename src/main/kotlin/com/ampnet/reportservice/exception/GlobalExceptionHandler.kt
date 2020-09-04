package com.ampnet.reportservice.exception

import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object : KLogging()

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceDoesNotExists(exception: ResourceNotFoundException): ErrorResponse {
        logger.error("ResourceNotFoundException", exception)
        return generateErrorResponse(exception.errorCode, exception.message)
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(InternalException::class)
    fun handleInternalException(exception: InternalException): ErrorResponse {
        logger.error("InternalException", exception)
        return generateErrorResponse(exception.errorCode, exception.message)
    }

    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    @ExceptionHandler(GrpcException::class)
    fun handleGrpcException(exception: GrpcException): ErrorResponse {
        logger.error("GrpcException", exception)
        return generateErrorResponse(exception.errorCode, exception.message)
    }

    private fun generateErrorResponse(errorCode: ErrorCode, systemMessage: String?): ErrorResponse {
        val errorMessage = systemMessage ?: "Error not defined"
        val errCode = errorCode.categoryCode + errorCode.specificCode
        return ErrorResponse(errorCode.message, errCode, errorMessage)
    }
}