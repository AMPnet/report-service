package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.controller.pojo.XlsxType
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
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class XlsxServiceImpl(
    private val userService: UserService,
    private val walletService: WalletService
) : XlsxService {

    @Suppress("MagicNumber")
    private val headerFontSize: Short = 16
    @Suppress("MagicNumber")
    private val dataFontSize: Short = 14

    private lateinit var workbook: XSSFWorkbook
    private lateinit var sheet: XSSFSheet

    override fun generateXlsx(coop: String, type: XlsxType): ByteArray {
        workbook = XSSFWorkbook()
        val users = getUsers(coop, type)
        writeHeaderLine(type)
        writeDataLines(users)
        val out = ByteArrayOutputStream()
        workbook.write(out)
        out.close()
        workbook.close()
        return out.toByteArray()
    }

    private fun getUsers(coop: String, type: XlsxType): List<UserResponse> =
        when (type) {
            XlsxType.REGISTERED -> userService.getAllUsers(coop)
            XlsxType.VERIFIED -> mapUserResponse(userService.getAllActiveUsers(coop).usersList)
            XlsxType.WALLET -> {
                val activeUsers = userService.getAllActiveUsers(coop)
                val walletOwners = walletService
                    .getWalletsByOwner(activeUsers.usersList.map { UUID.fromString(it.uuid) })
                    .map { it.uuid }
                mapUserResponse(activeUsers.usersList.filter { walletOwners.contains(it.uuid) })
            }
            XlsxType.DEPOSIT -> {
                val depositOwners = walletService.getOwnersWithApprovedDeposit(coop)
                val usersWithDeposit = userService.getAllActiveUsers(coop).usersList
                    .filter { depositOwners.contains(it.uuid) }
                mapUserResponse(usersWithDeposit)
            }
            XlsxType.INVESTMENT -> {
                val activeUsers = userService.getAllActiveUsers(coop)
                val walletOwners = walletService
                    .getWalletsByOwner(activeUsers.usersList.map { UUID.fromString(it.uuid) })
                    .map { it.uuid }
                mapUserResponse(userService.getAllActiveUsers(coop).usersList)
            }
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
            createCell(row, columnCount, dateToString(user.createdAt), style)
        }
    }

    private fun createCell(row: Row, columnCount: Int, value: String, style: CellStyle) {
        sheet.autoSizeColumn(columnCount)
        row.createCell(columnCount).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private fun dateToString(date: Long): String =
        date.millisecondsToLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    private fun mapUserResponse(users: List<UserExtendedResponse>): List<UserResponse> =
        users.map {
            UserResponse.newBuilder()
                .setUuid(it.uuid)
                .setEmail(it.email)
                .setFirstName(it.firstName)
                .setLastName(it.lastName)
                .setAuth(it.auth)
                .setCreatedAt(it.createdAt)
                .build()
        }
}
