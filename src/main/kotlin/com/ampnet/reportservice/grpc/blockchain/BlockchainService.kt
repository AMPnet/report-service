package com.ampnet.reportservice.grpc.blockchain

import com.ampnet.crowdfunding.proto.TransactionsResponse

interface BlockchainService {
    fun getTransactions(walletAddress: String): List<TransactionsResponse.Transaction>
}
