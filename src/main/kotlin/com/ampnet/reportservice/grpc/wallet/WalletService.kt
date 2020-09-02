package com.ampnet.reportservice.grpc.wallet

import com.ampnet.walletservice.proto.WalletResponse
import java.util.UUID

interface WalletService {
    fun getWallet(uuid: UUID): WalletResponse?
}
