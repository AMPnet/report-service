package com.ampnet.reportservice.grpc.blockchain

import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.crowdfunding.proto.UserWalletsForCoopAndTxTypeResponse

interface BlockchainService {
    fun getTransactions(walletHash: String): List<TransactionInfo>
    fun getTransactionInfo(txHash: String, fromTxHash: String, toTxHash: String): TransactionInfo
    fun getUserWalletsWithInvestment(coop: String): List<UserWalletsForCoopAndTxTypeResponse.WalletWithHash>
}
