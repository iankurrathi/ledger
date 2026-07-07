package com.bank

import com.bank.repository.AccountRepository
import com.bank.repository.TransactionRepository
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module(service: LedgerService = LedgerService(AccountRepository(), TransactionRepository())) {
    install(ContentNegotiation) { json() }
    configureRouting(service)
}
