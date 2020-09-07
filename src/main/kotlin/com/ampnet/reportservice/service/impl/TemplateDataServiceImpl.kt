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
import com.ampnet.reportservice.service.pojo.Transaction
import com.ampnet.reportservice.service.pojo.Transactions
import com.ampnet.userservice.proto.UserResponse
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

    private val platformWalletName = "Platform"

    companion object : KLogging()

    override fun getUserTransactionsData(userUUID: UUID): Transactions {
        val wallet = walletService.getWalletsByOwner(listOf(userUUID)).firstOrNull()
            ?: throw ResourceNotFoundException(ErrorCode.WALLET_MISSING, "Missing wallet for user with uuid: $userUUID")
        val transactions = blockchainService.getTransactions(wallet.hash)
        val walletHashes = getWalletHashes(transactions)
        val wallets = walletService.getWalletsByHash(walletHashes)
        return Transactions(
            setBlockchainTransactionFromToNames(transactions, wallets)
        )
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
        val users = userService.getUsers(walletOwners.map { UUID.fromString(it) }.toSet())
            .associateBy { it.uuid }
        val projects = projectService.getProjects(walletOwners.map { UUID.fromString(it) })
            .associateBy { it.uuid }
        val transactions = transactionsResponse.map { Transaction(it) }
        transactions.forEach { transaction ->
            val ownerUuidFrom = walletsMap[transaction.fromTxHash]?.owner
            val ownerUuidTo = walletsMap[transaction.toTxHash]?.owner
            when (transaction.type) {
                TransactionsResponse.Transaction.Type.INVEST -> {
                    transaction.from = getUserNameWithUuid(ownerUuidFrom, users)
                    transaction.to = getProjectNameWithUuid(ownerUuidTo, projects)
                }
                TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT -> {
                    transaction.from = getProjectNameWithUuid(ownerUuidFrom, projects)
                    transaction.to = getUserNameWithUuid(ownerUuidTo, users)
                }
                TransactionsResponse.Transaction.Type.SHARE_PAYOUT -> {
                    transaction.from = getProjectNameWithUuid(ownerUuidFrom, projects)
                    transaction.to = getUserNameWithUuid(ownerUuidTo, users)
                }
                TransactionsResponse.Transaction.Type.DEPOSIT -> {
                    transaction.from = platformWalletName
                    transaction.to = getUserNameWithUuid(ownerUuidTo, users)
                }
                TransactionsResponse.Transaction.Type.WITHDRAW -> {
                    transaction.from = getUserNameWithUuid(ownerUuidFrom, users)
                    transaction.to = platformWalletName
                }
                TransactionsResponse.Transaction.Type.UNRECOGNIZED -> {
                    // skip
                }
            }
        }
        return transactions
    }

    private fun getUserNameWithUuid(ownerUuid: String?, users: Map<String, UserResponse>): String? {
        return users[ownerUuid]?.let { user ->
            "${user.firstName} ${user.lastName}"
        }
    }

    private fun getProjectNameWithUuid(ownerUuid: String?, projects: Map<String, ProjectResponse>): String? {
        return projects[ownerUuid]?.name
    }
}
