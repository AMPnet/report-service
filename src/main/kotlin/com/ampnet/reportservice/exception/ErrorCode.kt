package com.ampnet.reportservice.exception

enum class ErrorCode(val categoryCode: String, var specificCode: String, var message: String) {
    // Internal: 08
    INT_GRPC_BLOCKCHAIN("08", "03", "Failed gRPC call to blockchain service"),
    INT_GRPC_WALLET("08", "09", "Failed gRPC call to wallet service")
}
