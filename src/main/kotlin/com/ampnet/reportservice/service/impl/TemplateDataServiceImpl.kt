package com.ampnet.reportservice.service.impl

import com.ampnet.core.jwt.UserPrincipal
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
import com.ampnet.reportservice.service.TranslationService
import com.ampnet.reportservice.service.data.SingleTransactionSummary
import com.ampnet.reportservice.service.data.Transaction
import com.ampnet.reportservice.service.data.TransactionCancelInvestment
import com.ampnet.reportservice.service.data.TransactionDeposit
import com.ampnet.reportservice.service.data.TransactionFactory
import com.ampnet.reportservice.service.data.TransactionInvest
import com.ampnet.reportservice.service.data.TransactionSharePayout
import com.ampnet.reportservice.service.data.TransactionWithdraw
import com.ampnet.reportservice.service.data.TransactionsSummary
import com.ampnet.reportservice.service.data.Translations
import com.ampnet.reportservice.service.data.UserInfo
import com.ampnet.reportservice.service.data.UsersAccountsSummary
import com.ampnet.userservice.proto.UsersExtendedResponse
import com.ampnet.walletservice.proto.WalletResponse
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.streams.asSequence

@Service
@Suppress("TooManyFunctions")
class TemplateDataServiceImpl(
    private val walletService: WalletService,
    private val blockchainService: BlockchainService,
    private val userService: UserService,
    private val projectService: ProjectService,
    private val translationService: TranslationService
) : TemplateDataService {

    companion object : KLogging()

    override fun getUserTransactionsData(userUUID: UUID, periodRequest: PeriodServiceRequest): TransactionsSummary {
        val wallet = getWalletByUser(userUUID)
        val transactions = blockchainService.getTransactions(wallet.hash)
            .filter { inTimePeriod(periodRequest, it.date) }
        val walletHashes = getWalletHashes(transactions)
        val wallets = walletService.getWalletsByHash(walletHashes)
        val userWithInfo = UserInfo(userService.getUserWithInfo(userUUID))
        val translations = translationService.getTranslations(userWithInfo.language)
        val transactionsWithNames =
            setBlockchainTransactionFromToNames(transactions, wallets, userWithInfo.language, translations)
        return TransactionsSummary(transactionsWithNames, userWithInfo, periodRequest, translations)
    }

    override fun getUserTransactionData(txServiceRequest: TransactionServiceRequest): SingleTransactionSummary {
        val user = txServiceRequest.userUuid
        val txHash = txServiceRequest.txHash
        val fromTxHash = txServiceRequest.fromTxHash
        val toTxHash = txServiceRequest.toTxHash
        validateTransactionBelongsToUser(getWalletByUser(user), fromTxHash, toTxHash)
        val transaction = blockchainService.getTransactionInfo(txHash, fromTxHash, toTxHash)
        val wallets = walletService.getWalletsByHash(setOf(fromTxHash, toTxHash))
        val userWithInfo = UserInfo(userService.getUserWithInfo(user))
        val translations = translationService.getTranslations(userWithInfo.language)
        val mappedTransaction =
            setBlockchainTransactionFromToNames(
                listOf(transaction), wallets, userWithInfo.language, translations
            ).firstOrNull()
                ?: throw InvalidRequestException(
                    ErrorCode.INT_UNSUPPORTED_TX, "Transaction with hash:$txHash is not supported in report"
                )
        return SingleTransactionSummary(mappedTransaction, userWithInfo, translations)
    }

    override fun getAllActiveUsersSummaryData(
        user: UserPrincipal,
        periodRequest: PeriodServiceRequest
    ): UsersAccountsSummary {
        val users = userService.getAllActiveUsers(user.coop)
        throwExceptionIfNoUserWithInfo(users)
        val reportLanguage = userService.getUsers(setOf(user.uuid)).firstOrNull()?.language.orEmpty()
        val userWallets = walletService.getWalletsByOwner(users.usersList.map { UUID.fromString(it.uuid) })
        val userTransactions = userWallets.parallelStream().asSequence().associateBy(
            { it.owner },
            { blockchainService.getTransactions(it.hash).filter { tx -> inTimePeriod(periodRequest, tx.date) } }
        )
        val translations = translationService.getTranslations(reportLanguage)
        val transactionsSummaryList = users.usersList.map { userResponse ->
            val transactions = getTransactions(userTransactions, userResponse.uuid)
            val userInfo = UserInfo(userResponse, users.coop, reportLanguage)
            TransactionsSummary(transactions, userInfo, periodRequest, translations)
        }
        return UsersAccountsSummary(transactionsSummaryList, users.coop.logo)
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
        wallets: List<WalletResponse>,
        language: String,
        translations: Translations
    ): List<Transaction> {
        val walletOwners = wallets.map { it.owner }
        val walletsMap = wallets.associateBy { it.hash }
        val projects = projectService.getProjects(walletOwners.map { UUID.fromString(it) })
            .associateBy { it.uuid }
        val transactions = transactionsResponse.mapNotNull { TransactionFactory.createTransaction(it) }
        transactions.forEach { transaction ->
            transaction.setLanguage(language)
            transaction.translations = translations
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
        // activationData is used only temporary to enable admin transactions.
        // When coop_id gets integrated in blockchain service,
        // all accounts are going to have tx_hash data (including admin) and then we'll standardize these calls
        val activationData = userWallet.activationData
        val hashes = listOf(fromTxHash, toTxHash)
        if (txHash !in hashes && activationData !in hashes)
            throw InvalidRequestException(
                ErrorCode.INT_REQUEST, "Transaction doesn't belong to user wallet with hash: $txHash"
            )
    }

    private fun getTransactions(
        userTransactions: Map<String, List<TransactionInfo>>,
        userUuid: String
    ): List<Transaction> =
        userTransactions[userUuid]?.mapNotNull { TransactionFactory.createTransaction(it) }.orEmpty()

    private fun throwExceptionIfNoUserWithInfo(users: UsersExtendedResponse) {
        if (users.usersList.isEmpty())
            throw ResourceNotFoundException(ErrorCode.USER_MISSING_INFO, "No user has went through kyc on the platform")
    }
}
