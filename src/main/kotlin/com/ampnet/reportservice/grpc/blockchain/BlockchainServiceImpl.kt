package com.ampnet.reportservice.grpc.blockchain

import com.ampnet.crowdfunding.proto.BlockchainServiceGrpc
import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.crowdfunding.proto.TransactionInfoRequest
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.crowdfunding.proto.TransactionsRequest
import com.ampnet.crowdfunding.proto.UserWalletsForCoopAndTxTypeRequest
import com.ampnet.reportservice.config.ApplicationProperties
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.GrpcException
import com.ampnet.reportservice.exception.GrpcHandledException
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws
import com.ampnet.crowdfunding.proto.UserWalletsForCoopAndTxTypeResponse.WalletWithHash as WalletWithHash

@Service
class BlockchainServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : BlockchainService {

    companion object : KLogging()

    private val blockchainService: BlockchainServiceGrpc.BlockchainServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("blockchain-service")
        BlockchainServiceGrpc.newBlockingStub(channel)
    }

    @Throws(GrpcException::class)
    override fun getTransactions(walletHash: String): List<TransactionInfo> {
        logger.debug { "Get transactions for wallet address: $walletHash" }
        if (walletHash.isBlank()) return emptyList()
        try {
            val response = serviceWithTimeout()
                .getTransactions(
                    TransactionsRequest.newBuilder()
                        .setWalletHash(walletHash)
                        .build()
                )
            logger.debug { "Transactions response: ${response.transactionsCount}" }
            return response.transactionsList
        } catch (ex: StatusRuntimeException) {
            throw getInternalExceptionFromStatusException(ex, "Could not get transactions for wallet: $walletHash")
        }
    }

    @Throws(GrpcException::class)
    override fun getTransactionInfo(txHash: String, fromTxHash: String, toTxHash: String): TransactionInfo {
        logger.debug { "Get info for transaction with hash: $txHash" }
        try {
            val response = serviceWithTimeout()
                .getTransactionInfo(
                    TransactionInfoRequest.newBuilder()
                        .setTxHash(txHash)
                        .setFrom(fromTxHash)
                        .setTo(toTxHash)
                        .build()
                )
            logger.debug { "TransactionInfoResponse response: $response" }
            return response
        } catch (ex: StatusRuntimeException) {
            throw getInternalExceptionFromStatusException(ex, "Could not get info for transaction with hash: $txHash")
        }
    }

    @Throws(GrpcException::class)
    override fun getUserWalletsWithInvestment(coop: String): List<WalletWithHash> {
        logger.debug { "Get user wallets with investment for coop: $coop" }
        try {
            val response = serviceWithTimeout()
                .getUserWalletsForCoopAndTxType(
                    UserWalletsForCoopAndTxTypeRequest.newBuilder()
                        .setCoop(coop)
                        .setType(TransactionType.INVEST)
                        .build()
                )
            logger.debug { "TransactionInfoResponse response: $response" }
            return response.walletsList
        } catch (ex: StatusRuntimeException) {
            throw getInternalExceptionFromStatusException(
                ex,
                "Could not get user wallets with investment for coop: $coop"
            )
        }
    }

    private fun serviceWithTimeout() = blockchainService
        .withDeadlineAfter(applicationProperties.grpc.blockchainServiceTimeout, TimeUnit.MILLISECONDS)

    private fun getInternalExceptionFromStatusException(
        ex: StatusRuntimeException,
        message: String
    ): GrpcException {
        val grpcErrorCode = getErrorDescriptionFromExceptionStatus(ex)
            ?: return GrpcException(ErrorCode.INT_GRPC_BLOCKCHAIN, ex.localizedMessage)
        val errorCode = ErrorCode.MIDDLEWARE
        errorCode.specificCode = grpcErrorCode.code
        errorCode.message = grpcErrorCode.message
        return GrpcHandledException(errorCode, message)
    }

    // Status defined in ampenet-blockchain service, for more info see:
    // ampnet-blockchain-service/src/main/kotlin/com/ampnet/crowdfunding/blockchain/enums/ErrorCode.kt
    private fun getErrorDescriptionFromExceptionStatus(ex: StatusRuntimeException): GrpcErrorCode? {
        val description = ex.status.description?.split(" > ")
        if (description?.size != 2) return null
        return GrpcErrorCode(description[0], description[1])
    }

    private data class GrpcErrorCode(val code: String, val message: String)
}
