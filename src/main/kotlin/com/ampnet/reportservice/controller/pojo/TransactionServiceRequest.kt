package com.ampnet.reportservice.controller.pojo

import java.util.UUID

data class TransactionServiceRequest(
    val userUuid: UUID,
    val txHash: String,
    val fromTxHash: String,
    val toTxHash: String
)
