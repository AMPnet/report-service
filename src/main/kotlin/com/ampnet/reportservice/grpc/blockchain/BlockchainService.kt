package com.ampnet.reportservice.grpc.blockchain

import com.ampnet.crowdfunding.proto.TransactionInfo

interface BlockchainService {
    fun getTransactions(walletHash: String): List<TransactionInfo>
    fun getTransactionInfo(txHash: String, fromTxHash: String, toTxHash: String): TransactionInfo
}
