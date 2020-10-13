package com.ampnet.reportservice.grpc.blockchain

import com.ampnet.crowdfunding.proto.BlockchainServiceGrpc
import com.ampnet.crowdfunding.proto.TransactionsRequest
import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.config.ApplicationProperties
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.GrpcException
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

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

    override fun getTransactions(walletHash: String): List<TransactionsResponse.Transaction> {
        logger.debug { "Get transactions for wallet hash: $walletHash" }
        try {
            val response = serviceWithTimeout()
                .getTransactions(
                    TransactionsRequest.newBuilder()
                        .setTxHash(walletHash)
                        .build()
                )
            logger.debug { "Transactions response: $response" }
            return response.transactionsList
        } catch (ex: StatusRuntimeException) {
            throw getInternalExceptionFromStatusException(ex, "Could not get transactions for wallet: $walletHash")
        }
    }

    private fun serviceWithTimeout() = blockchainService
        .withDeadlineAfter(applicationProperties.grpc.blockchainServiceTimeout, TimeUnit.MILLISECONDS)

    private fun getInternalExceptionFromStatusException(ex: StatusRuntimeException, message: String): GrpcException {
        val grpcErrorCode = getErrorDescriptionFromExceptionStatus(ex)
            ?: return GrpcException(ErrorCode.INT_GRPC_BLOCKCHAIN, "Failed call to blockchain service", ex)
        val errorCode = ErrorCode.INT_GRPC_BLOCKCHAIN
        errorCode.specificCode = grpcErrorCode.code
        errorCode.message = grpcErrorCode.message
        return GrpcException(errorCode, message, ex)
    }

    // Status defined in ampenet-blockchain service, for more info see:
    // ampnet-blockchain-service/src/main/kotlin/com/ampnet/crowdfunding/blockchain/enums/ErrorCode.kt
    private fun getErrorDescriptionFromExceptionStatus(ex: StatusRuntimeException): GrpcErrorCode? =
        ex.status.description?.split(" > ")?.let { description ->
            if (description.size == 2) {
                GrpcErrorCode(description[0], description[1])
            } else {
                null
            }
        }

    private data class GrpcErrorCode(val code: String, val message: String)
}
