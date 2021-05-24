package com.ampnet.reportservice.service

import com.ampnet.reportservice.controller.pojo.XlsxType

interface XlsxService {
    fun generateXlsx(coop: String, type: XlsxType): ByteArray
}
