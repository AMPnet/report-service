package com.ampnet.reportservice.grpc.wallet

import com.ampnet.reportservice.config.ApplicationProperties
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.GrpcException
import com.ampnet.walletservice.proto.GetWalletsByOwnerRequest
import com.ampnet.walletservice.proto.WalletResponse
import com.ampnet.walletservice.proto.WalletServiceGrpc
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

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

    override fun getWallet(uuid: UUID): WalletResponse? {
        logger.debug { "Fetching wallet: $uuid" }
        try {
            val request = GetWalletsByOwnerRequest.newBuilder()
                .addAllOwnersUuids(listOf(uuid.toString()))
                .build()
            val response = serviceWithTimeout()
                .getWallets(request).walletsList
            return response.firstOrNull()?.let { wallet ->
                logger.debug { "Fetched wallet: $wallet" }
                wallet
            }
        } catch (ex: StatusRuntimeException) {
            throw GrpcException(ErrorCode.INT_GRPC_WALLET, "Failed to fetch wallets. ${ex.localizedMessage}")
        }
    }

    private fun serviceWithTimeout() = walletServiceStub
        .withDeadlineAfter(applicationProperties.grpc.walletServiceTimeout, TimeUnit.MILLISECONDS)
}
