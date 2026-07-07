package com.bank

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module(service: LedgerService = LedgerService(LedgerRepository())) {
    install(ContentNegotiation) { json() }
    configureRouting(service)
}
