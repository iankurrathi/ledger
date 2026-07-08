package com.bank

import com.bank.dto.BalanceResponse
import com.bank.dto.TransactionRequest
import com.bank.dto.toResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.uuid.Uuid

fun Application.configureRouting(service: LedgerService) {
    routing {
        post("/accounts") {
            call.respond(HttpStatusCode.Created, service.createAccount())
        }

        put("/accounts/{accountUid}/transactions/{transactionUid}") {
            val accountUid = Uuid.parse(call.parameters["accountUid"]!!)
            val transactionUid = Uuid.parse(call.parameters["transactionUid"]!!)
            val body = call.receive<TransactionRequest>()
            when (val result = service.putTransaction(accountUid, transactionUid, body.type, body.amount)) {
                is LedgerService.PutTransactionResult.Success ->
                    call.respond(HttpStatusCode.OK, result.transaction.toResponse())
                LedgerService.PutTransactionResult.AccountNotFound ->
                    call.respond(HttpStatusCode.NotFound)
                LedgerService.PutTransactionResult.Conflict ->
                    call.respond(HttpStatusCode.Conflict)
                LedgerService.PutTransactionResult.InsufficientFunds ->
                    call.respond(HttpStatusCode.UnprocessableEntity)
            }
        }

        get("/accounts/{accountUid}/balance") {
            val accountUid = Uuid.parse(call.parameters["accountUid"]!!)
            val balance = service.getBalance(accountUid)
            if (balance == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(BalanceResponse(balance))
        }

        get("/accounts/{accountUid}/transactions") {
            val accountUid = Uuid.parse(call.parameters["accountUid"]!!)
            val txs = service.getTransactions(accountUid)
            if (txs == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(txs.map { it.toResponse() })
        }
    }
}
