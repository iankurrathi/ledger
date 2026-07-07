package com.bank

import io.ktor.server.application.*
import io.ktor.util.*

val LedgerServiceKey = AttributeKey<LedgerService>("LedgerService")

fun Application.configureLedger() {
    attributes.put(LedgerServiceKey, LedgerService(LedgerRepository()))
}
