package com.bank

import com.bank.models.TransactionType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    val service = attributes[LedgerServiceKey]
    routing {
        post("/accounts") {
            call.respond(HttpStatusCode.Created, service.createAccount())
        }

        put("/accounts/{accountUid}/transactions/{transactionUid}") {
            val accountUid = call.parameters["accountUid"]!!
            val transactionUid = call.parameters["transactionUid"]!!
            val body = call.receive<TransactionRequest>()
            when (val result = service.putTransaction(accountUid, transactionUid, body.type, body.amount)) {
                is LedgerService.PutTransactionResult.Success ->
                    call.respond(HttpStatusCode.OK, result.transaction)
                LedgerService.PutTransactionResult.AccountNotFound ->
                    call.respond(HttpStatusCode.NotFound)
                LedgerService.PutTransactionResult.Conflict ->
                    call.respond(HttpStatusCode.Conflict)
                LedgerService.PutTransactionResult.InsufficientFunds ->
                    call.respond(HttpStatusCode.UnprocessableEntity)
            }
        }

        get("/accounts/{accountUid}/balance") {
            val accountUid = call.parameters["accountUid"]!!
            val balance = service.getBalance(accountUid)
            if (balance == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(BalanceResponse(balance))
        }

        get("/accounts/{accountUid}/transactions") {
            val accountUid = call.parameters["accountUid"]!!
            val txs = service.getTransactions(accountUid)
            if (txs == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(txs)
        }
    }
}

@Serializable
private data class TransactionRequest(val type: TransactionType, val amount: Long)

@Serializable
data class BalanceResponse(val balance: Long)
