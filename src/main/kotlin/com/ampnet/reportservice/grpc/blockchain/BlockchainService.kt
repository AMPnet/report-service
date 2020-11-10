package com.ampnet.reportservice.grpc.blockchain

import com.ampnet.crowdfunding.proto.TransactionResponse

interface BlockchainService {
    fun getTransactions(walletAddress: String): List<TransactionResponse>
    fun getTransactionInfo(txHash: String, fromTxHash: String, toTxHash: String): TransactionResponse
}
