package com.ampnet.reportservice.exception

open class GrpcException(val errorCode: ErrorCode, exceptionMessage: String) : Exception(exceptionMessage)
class GrpcHandledException(errorCode: ErrorCode, exceptionMessage: String) : GrpcException(errorCode, exceptionMessage)

class ResourceNotFoundException(val errorCode: ErrorCode, exceptionMessage: String) : Exception(exceptionMessage)

class InternalException(val errorCode: ErrorCode, exceptionMessage: String, throwable: Throwable? = null) :
    Exception(exceptionMessage, throwable)

class InvalidRequestException(
    val errorCode: ErrorCode,
    exceptionMessage: String,
    throwable: Throwable? = null
) : Exception(exceptionMessage, throwable)
