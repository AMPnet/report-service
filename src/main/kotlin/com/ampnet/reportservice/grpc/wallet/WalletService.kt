package com.ampnet.reportservice.grpc.wallet

import com.ampnet.walletservice.proto.WalletResponse
import java.util.UUID

interface WalletService {
    fun getWalletsByOwner(uuids: List<UUID>): List<WalletResponse>
    fun getWalletsByHash(hashes: Set<String>): List<WalletResponse>
}
