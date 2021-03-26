package com.ampnet.reportservice.grpc.wallet

import com.ampnet.reportservice.config.ApplicationProperties
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.GrpcException
import com.ampnet.walletservice.proto.GetWalletsByHashRequest
import com.ampnet.walletservice.proto.GetWalletsByOwnerRequest
import com.ampnet.walletservice.proto.WalletResponse
import com.ampnet.walletservice.proto.WalletServiceGrpc
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

@Service
class WalletServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : WalletService {

    companion object : KLogging()

    private val walletServiceStub: WalletServiceGrpc.WalletServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("wallet-service")
        WalletServiceGrpc.newBlockingStub(channel)
    }

    @Throws(GrpcException::class)
    override fun getWalletsByOwner(uuids: List<UUID>): List<WalletResponse> {
        logger.debug { "Fetching wallets by owner uuid: $uuids" }
        try {
            val request = GetWalletsByOwnerRequest.newBuilder()
                .addAllOwnersUuids(uuids.map { it.toString() })
                .build()
            val response = serviceWithTimeout()
                .getWalletsByOwner(request).walletsList
            logger.debug { "Fetched wallets: $response" }
            return response
        } catch (ex: StatusRuntimeException) {
            logger.warn(ex.localizedMessage)
            throw GrpcException(ErrorCode.INT_GRPC_WALLET, "Failed to fetch wallets")
        }
    }

    @Throws(GrpcException::class)
    override fun getWalletsByHash(hashes: Set<String>): List<WalletResponse> {
        logger.debug { "Fetching wallets by hashes: $hashes" }
        try {
            val request = GetWalletsByHashRequest.newBuilder()
                .addAllHashes(hashes)
                .build()
            val response = serviceWithTimeout()
                .getWalletsByHash(request).walletsList
            logger.debug { "Fetched wallets: $response" }
            return response
        } catch (ex: StatusRuntimeException) {
            logger.warn(ex.localizedMessage)
            throw GrpcException(ErrorCode.INT_GRPC_WALLET, "Failed to fetch wallets")
        }
    }

    private fun serviceWithTimeout() = walletServiceStub
        .withDeadlineAfter(applicationProperties.grpc.walletServiceTimeout, TimeUnit.MILLISECONDS)
}
