package com.ampnet.reportservice.exception

class GrpcException(val errorCode: ErrorCode, exceptionMessage: String, throwable: Throwable) :
    Exception(exceptionMessage, throwable)

class ResourceNotFoundException(val errorCode: ErrorCode, exceptionMessage: String) : Exception(exceptionMessage)

class InternalException(val errorCode: ErrorCode, exceptionMessage: String, throwable: Throwable? = null) :
    Exception(exceptionMessage, throwable)
