package com.ampnet.reportservice.exception

enum class ErrorCode(val categoryCode: String, var specificCode: String, var message: String) {

    // Users: 03
    USER_MISSING_PRIVILEGE("03", "05", "Missing privilege to access data"),

    // Wallet: 05
    WALLET_MISSING("05", "01", "Missing wallet"),

    // Internal: 08
    INT_GRPC_BLOCKCHAIN("08", "03", "Failed gRPC call to blockchain service"),
    INT_GRPC_USER("08", "04", "Failed gRPC call to user service"),
    INT_GRPC_PROJECT("08", "05", "Failed gRPC call to project service"),
    INT_REQUEST("08", "08", "Invalid controller request exception"),
    INT_GRPC_WALLET("08", "09", "Failed gRPC call to wallet service"),
    INT_GENERATING_PDF("08", "10", "Could not generate pdf from data"),
    INT_UNSUPPORTED_TX("08", "11", "Unsupported transaction"),

    // Middleware: 11
    MIDDLEWARE("11", "00", "Undefined")
}
