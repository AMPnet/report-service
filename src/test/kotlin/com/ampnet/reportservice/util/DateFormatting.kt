package com.ampnet.reportservice.util

import java.time.LocalDateTime
import java.time.ZoneOffset

fun LocalDateTime.toMiliSeconds(): String =
    this.toInstant(ZoneOffset.UTC).toEpochMilli().toString()
