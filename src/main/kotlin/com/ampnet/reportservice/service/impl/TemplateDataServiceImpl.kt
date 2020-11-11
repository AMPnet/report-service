package com.ampnet.reportservice.service.impl

import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.TransactionServiceRequest
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InvalidRequestException
import com.ampnet.reportservice.exception.ResourceNotFoundException
import com.ampnet.reportservice.grpc.blockchain.BlockchainService
import com.ampnet.reportservice.grpc.projectservice.ProjectService
import com.ampnet.reportservice.grpc.userservice.UserService
import com.ampnet.reportservice.grpc.wallet.WalletService
import com.ampnet.reportservice.service.TemplateDataService
import com.ampnet.reportservice.service.data.SingleTransactionSummary
import com.ampnet.reportservice.service.data.Transaction
import com.ampnet.reportservice.service.data.TransactionCancelInvestment
import com.ampnet.reportservice.service.data.TransactionDeposit
import com.ampnet.reportservice.service.data.TransactionFactory
import com.ampnet.reportservice.service.data.TransactionInvest
import com.ampnet.reportservice.service.data.TransactionSharePayout
import com.ampnet.reportservice.service.data.TransactionWithdraw
import com.ampnet.reportservice.service.data.TransactionsSummary
import com.ampnet.reportservice.service.data.UserInfo
import com.ampnet.walletservice.proto.WalletResponse
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Service
class TemplateDataServiceImpl(
    private val walletService: WalletService,
    private val blockchainService: BlockchainService,
    private val userService: UserService,
    private val projectService: ProjectService
) : TemplateDataService {

    companion object : KLogging()

    override fun getUserTransactionsData(userUUID: UUID, periodRequest: PeriodServiceRequest): TransactionsSummary {
        val wallet = getWalletByUser(userUUID)
        val transactions = blockchainService.getTransactions(wallet.activationData)
            .filter { inTimePeriod(periodRequest, it.date) }
        val walletHashes = getWalletHashes(transactions)
        val wallets = walletService.getWalletsByHash(walletHashes)
        val userWithInfo = UserInfo(userUUID, userService.getUserWithInfo(userUUID))
        return TransactionsSummary(
            setBlockchainTransactionFromToNames(transactions, wallets), userWithInfo, periodRequest
        )
    }

    override fun getUserTransactionData(txServiceRequest: TransactionServiceRequest): SingleTransactionSummary {
        val user = txServiceRequest.userUuid
        val txHash = txServiceRequest.txHash
        val fromTxHash = txServiceRequest.fromTxHash
        val toTxHash = txServiceRequest.toTxHash
        validateTransactionBelongsToUser(getWalletByUser(user), fromTxHash, toTxHash)
        val transaction = blockchainService.getTransactionInfo(txHash, fromTxHash, toTxHash)
        val wallets = walletService.getWalletsByHash(setOf(fromTxHash, toTxHash))
        val userWithInfo = UserInfo(user, userService.getUserWithInfo(user))
        val mappedTransaction = setBlockchainTransactionFromToNames(listOf(transaction), wallets).firstOrNull()
            ?: throw InvalidRequestException(
                ErrorCode.INT_UNSUPPORTED_TX, "Transaction with hash:$txHash is not supported in report"
            )
        return SingleTransactionSummary(mappedTransaction, userWithInfo)
    }

    private fun getWalletHashes(transactions: List<TransactionInfo>): Set<String> {
        val walletHashes: MutableSet<String> = mutableSetOf()
        transactions.forEach { transaction ->
            walletHashes.add(transaction.fromTxHash)
            walletHashes.add(transaction.toTxHash)
        }
        return walletHashes
    }

    private fun setBlockchainTransactionFromToNames(
        transactionsResponse: List<TransactionInfo>,
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
            when (transaction) {
                is TransactionInvest -> {
                    transaction.description = getProjectNameWithUuid(ownerUuidTo, projects)
                    getExpectedProjectFunding(ownerUuidTo, projects)?.let {
                        transaction.setPercentageInProject(it)
                    }
                }
                is TransactionCancelInvestment -> {
                    transaction.description = getProjectNameWithUuid(ownerUuidFrom, projects)
                    getExpectedProjectFunding(ownerUuidFrom, projects)?.let {
                        transaction.setPercentageInProject(it)
                    }
                }
                is TransactionSharePayout -> {
                    transaction.description = getProjectNameWithUuid(ownerUuidFrom, projects)
                }
                is TransactionDeposit -> {
                    // skip
                }
                is TransactionWithdraw -> {
                    // skip
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

    private fun inTimePeriod(periodRequest: PeriodServiceRequest, dateTime: String): Boolean {
        val date = dateTime.toLong()
        val fromDate = periodRequest.from?.toInstant(ZoneOffset.UTC)?.toEpochMilli() ?: 0
        val toDate = periodRequest.to?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
            ?: Instant.now().toEpochMilli()
        return date in fromDate..toDate
    }

    private fun getWalletByUser(userUuid: UUID): WalletResponse =
        walletService.getWalletsByOwner(listOf(userUuid)).firstOrNull()
            ?: throw ResourceNotFoundException(ErrorCode.WALLET_MISSING, "Missing wallet for user with uuid: $userUuid")

    private fun validateTransactionBelongsToUser(userWallet: WalletResponse, fromTxHash: String, toTxHash: String) {
        val txHash = userWallet.hash
        if (txHash != fromTxHash && txHash != toTxHash) {
            throw InvalidRequestException(
                ErrorCode.INT_REQUEST, "Transaction doesn't belong to user wallet with hash: $txHash"
            )
        }
    }
}
