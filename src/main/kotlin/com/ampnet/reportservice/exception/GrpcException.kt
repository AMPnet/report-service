package com.ampnet.reportservice.exception

class GrpcException(val errorCode: ErrorCode, exceptionMessage: String) : Exception(exceptionMessage)
