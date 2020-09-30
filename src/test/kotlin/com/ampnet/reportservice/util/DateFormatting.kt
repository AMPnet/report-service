package com.ampnet.reportservice.util

import java.time.LocalDateTime
import java.time.ZoneOffset

fun LocalDateTime.toMiliSeconds(): Long =
    this.toInstant(ZoneOffset.UTC).toEpochMilli()
