package com.ampnet.reportservice.grpc.blockchain

import com.ampnet.crowdfunding.proto.TransactionsResponse

interface BlockchainService {
    fun getTransactions(walletHash: String): List<TransactionsResponse.Transaction>
}
