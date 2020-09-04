package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.ResourceNotFoundException
import com.ampnet.reportservice.grpc.blockchain.BlockchainService
import com.ampnet.reportservice.grpc.wallet.WalletService
import com.ampnet.reportservice.service.TemplateDataService
import com.ampnet.reportservice.service.pojo.Transaction
import com.ampnet.reportservice.service.pojo.Transactions
import mu.KLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TemplateDataServiceImpl(
    private val walletService: WalletService,
    private val blockchainService: BlockchainService
) : TemplateDataService {

    companion object : KLogging()

    override fun getUserTransactionsData(userUUID: UUID): Transactions {
        val wallet = walletService.getWallet(userUUID)
            ?: throw ResourceNotFoundException(ErrorCode.WALLET_MISSING, "Missing wallet for owner: $userUUID")
        val transactions = blockchainService.getTransactions(wallet.hash)
        return Transactions(transactions.map { Transaction(it) })
    }
}
