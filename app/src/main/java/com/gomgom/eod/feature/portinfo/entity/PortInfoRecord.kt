package com.gomgom.eod.feature.portinfo.entity

data class PortInfoRecord(
    val recordId: Long,
    val country: String,
    val portName: String,
    val berthName: String,
    val cargoName: String,
    val note: String = ""
)