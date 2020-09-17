package com.ampnet.reportservice.service.impl

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.ResourceNotFoundException
import com.ampnet.reportservice.grpc.blockchain.BlockchainService
import com.ampnet.reportservice.grpc.projectservice.ProjectService
import com.ampnet.reportservice.grpc.userservice.UserService
import com.ampnet.reportservice.grpc.wallet.WalletService
import com.ampnet.reportservice.service.TemplateDataService
import com.ampnet.reportservice.service.data.Transaction
import com.ampnet.reportservice.service.data.TransactionFactory
import com.ampnet.reportservice.service.data.TxSummary
import com.ampnet.reportservice.service.data.UserInfo
import com.ampnet.walletservice.proto.WalletResponse
import mu.KLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TemplateDataServiceImpl(
    private val walletService: WalletService,
    private val blockchainService: BlockchainService,
    private val userService: UserService,
    private val projectService: ProjectService
) : TemplateDataService {

    companion object : KLogging()

    override fun getUserTransactionsData(userUUID: UUID): TxSummary {
        val wallet = walletService.getWalletsByOwner(listOf(userUUID)).firstOrNull()
            ?: throw ResourceNotFoundException(ErrorCode.WALLET_MISSING, "Missing wallet for user with uuid: $userUUID")
        val transactions = blockchainService.getTransactions(wallet.hash)
        val walletHashes = getWalletHashes(transactions)
        val wallets = walletService.getWalletsByHash(walletHashes)
        val userWithInfo = UserInfo(userUUID, userService.getUserWithInfo(userUUID))
        return TxSummary(setBlockchainTransactionFromToNames(transactions, wallets), userWithInfo)
    }

    private fun getWalletHashes(transactions: List<TransactionsResponse.Transaction>): Set<String> {
        val walletHashes: MutableSet<String> = mutableSetOf()
        transactions.forEach { transaction ->
            walletHashes.add(transaction.fromTxHash)
            walletHashes.add(transaction.toTxHash)
        }
        return walletHashes
    }

    private fun setBlockchainTransactionFromToNames(
        transactionsResponse: List<TransactionsResponse.Transaction>,
        wallets: List<WalletResponse>
    ): List<Transaction> {
        val walletOwners = wallets.map { it.owner }
        val walletsMap = wallets.associateBy { it.hash }
        // users will be needed for shares trading
        // val users = userService.getUsers(walletOwners.map { UUID.fromString(it) }.toSet())
        //     .associateBy { it.uuid }
        val projects = projectService.getProjects(walletOwners.map { UUID.fromString(it) })
            .associateBy { it.uuid }
        val transactions = transactionsResponse.mapNotNull { TransactionFactory.createTransaction(it) }
        transactions.forEach { transaction ->
            val ownerUuidFrom = walletsMap[transaction.fromTxHash]?.owner
            val ownerUuidTo = walletsMap[transaction.toTxHash]?.owner
            when (transaction.type) {
                TransactionsResponse.Transaction.Type.INVEST -> {
                    transaction.description = getProjectNameWithUuid(ownerUuidTo, projects)
                    getExpectedProjectFunding(ownerUuidTo, projects)?.let {
                        transaction.setPercentageInProject(it)
                    }
                }
                TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT -> {
                    transaction.description = getProjectNameWithUuid(ownerUuidFrom, projects)
                    getExpectedProjectFunding(ownerUuidFrom, projects)?.let {
                        transaction.setPercentageInProject(it)
                    }
                }
                TransactionsResponse.Transaction.Type.SHARE_PAYOUT -> {
                    transaction.description = getProjectNameWithUuid(ownerUuidFrom, projects)
                }
                TransactionsResponse.Transaction.Type.DEPOSIT, TransactionsResponse.Transaction.Type.WITHDRAW -> {
                    // from and to data not needed
                }
                TransactionsResponse.Transaction.Type.UNRECOGNIZED -> {
                    logger.warn { "Unrecognized transaction: $transaction" }
                }
            }
        }
        return transactions
    }

    // private fun getUserNameWithUuid(ownerUuid: String?, users: Map<String, UserResponse>): String? {
    //     return users[ownerUuid]?.let { user ->
    //         "${user.firstName} ${user.lastName}"
    //     }
    // }

    private fun getProjectNameWithUuid(ownerUuid: String?, projects: Map<String, ProjectResponse>): String? {
        return projects[ownerUuid]?.name
    }

    private fun getExpectedProjectFunding(ownerUuid: String?, projects: Map<String, ProjectResponse>): Long? {
        return projects[ownerUuid]?.expectedFunding
    }
}
