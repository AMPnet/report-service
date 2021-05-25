package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.controller.pojo.XlsxType
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InternalException
import com.ampnet.reportservice.grpc.blockchain.BlockchainService
import com.ampnet.reportservice.grpc.userservice.UserService
import com.ampnet.reportservice.grpc.wallet.WalletService
import com.ampnet.reportservice.service.XlsxService
import com.ampnet.reportservice.service.data.millisecondsToLocalDateTime
import com.ampnet.userservice.proto.UserExtendedResponse
import com.ampnet.userservice.proto.UserResponse
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.jvm.Throws

@Service
class XlsxServiceImpl(
    private val userService: UserService,
    private val walletService: WalletService,
    private val blockchainService: BlockchainService
) : XlsxService {

    companion object {
        const val headerFontSize: Short = 16
        const val dataFontSize: Short = 14
    }

    private lateinit var workbook: XSSFWorkbook
    private lateinit var sheet: XSSFSheet

    @Throws(InternalException::class)
    override fun generateXlsx(coop: String, type: XlsxType): ByteArray {
        try {
            workbook = XSSFWorkbook()
            val users = getUsers(coop, type)
            writeHeaderLine(type)
            writeDataLines(users)
            val outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()
            return outputStream.toByteArray()
        } catch (exception: IOException) {
            throw InternalException(ErrorCode.INT_GENERATING_XLSX, "Failed to generate xlsx file", exception)
        }
    }

    private fun getUsers(coop: String, type: XlsxType): List<UserResponse> =
        when (type) {
            XlsxType.REGISTERED -> userService.getAllUsers(coop)
            XlsxType.VERIFIED -> mapUserResponse(userService.getAllActiveUsers(coop).usersList)
            XlsxType.WALLET -> getUsersWithWallet(coop)
            XlsxType.DEPOSIT -> getUsersWithApprovedDeposit(coop)
            XlsxType.INVESTMENT -> getUsersWithInvestment(coop)
        }

    private fun getUsersWithInvestment(coop: String): List<UserResponse> {
        val activeUsers = userService.getAllActiveUsers(coop).usersList
        val walletsWithInvestment = blockchainService.getUserWalletsWithInvestment(coop).map { it.wallet }
        val walletOwners = walletService
            .getWalletsByOwner(activeUsers.map { UUID.fromString(it.uuid) })
            .filter { walletsWithInvestment.contains(it.activationData) }
            .map { it.owner }
        val usersWithInvestment = activeUsers.filter { walletOwners.contains(it.uuid) }
        return mapUserResponse(usersWithInvestment)
    }

    private fun getUsersWithApprovedDeposit(coop: String): List<UserResponse> {
        val depositOwners = walletService.getOwnersWithApprovedDeposit(coop)
        val usersWithDeposit = userService.getAllActiveUsers(coop).usersList
            .filter { depositOwners.contains(it.uuid) }
        return mapUserResponse(usersWithDeposit)
    }

    private fun getUsersWithWallet(coop: String): List<UserResponse> {
        val activeUsers = userService.getAllActiveUsers(coop)
        val walletOwners = walletService
            .getWalletsByOwner(activeUsers.usersList.map { UUID.fromString(it.uuid) })
            .map { it.owner }
        val usersWithWallet = activeUsers.usersList.filter { walletOwners.contains(it.uuid) }
        return mapUserResponse(usersWithWallet)
    }

    private fun writeHeaderLine(type: XlsxType) {
        sheet = workbook.createSheet("Users-$type")
        val font = workbook.createFont().apply {
            bold = true
            fontHeight = headerFontSize
        }
        val style = workbook.createCellStyle().apply { setFont(font) }
        val row = sheet.createRow(0)
        var columnCount = 0
        createCell(row, columnCount++, "User UUID", style)
        createCell(row, columnCount++, "E-mail", style)
        createCell(row, columnCount++, "First Name", style)
        createCell(row, columnCount++, "Last Name", style)
        createCell(row, columnCount++, "Authentication Method", style)
        createCell(row, columnCount, "Registration Date", style)
    }

    private fun writeDataLines(users: List<UserResponse>) {
        val font = workbook.createFont().apply { fontHeight = dataFontSize }
        val style = workbook.createCellStyle().apply { setFont(font) }
        var rowCount = 1
        for (user in users) {
            val row: Row = sheet.createRow(rowCount++)
            var columnCount = 0
            createCell(row, columnCount++, user.uuid, style)
            createCell(row, columnCount++, user.email, style)
            createCell(row, columnCount++, user.firstName, style)
            createCell(row, columnCount++, user.lastName, style)
            createCell(row, columnCount++, user.auth, style)
            createCell(row, columnCount, user.createdAt.toDateString(), style)
        }
    }

    private fun createCell(row: Row, columnCount: Int, value: String, style: CellStyle) {
        sheet.autoSizeColumn(columnCount)
        row.createCell(columnCount).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private fun mapUserResponse(users: List<UserExtendedResponse>): List<UserResponse> =
        users.map { user ->
            UserResponse.newBuilder().apply {
                uuid = user.uuid
                email = user.email
                firstName = user.firstName
                lastName = user.lastName
                auth = user.auth
                createdAt = user.createdAt
            }.build()
        }
}

fun Long.toDateString(): String {
    return this.millisecondsToLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}
