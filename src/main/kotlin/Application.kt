package com.bank

import com.bank.repository.AccountRepository
import com.bank.repository.TransactionRepository
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module(service: LedgerService = LedgerService(AccountRepository(), TransactionRepository())) {
    install(ContentNegotiation) { json() }
    install(StatusPages) {
        exception<IllegalArgumentException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, "Invalid request")
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, "Invalid request body")
        }
    }
    configureRouting(service)
}
