package com.bank

import com.bank.dto.TransactionResponse
import com.bank.models.TransactionType
import com.bank.repository.AccountRepository
import com.bank.repository.TransactionRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import kotlin.test.*
import kotlin.uuid.Uuid

class ApplicationShould {

    private fun ApplicationTestBuilder.setupApp(
        service: LedgerService = LedgerService(AccountRepository(), TransactionRepository())
    ) {
        application { module(service) }
    }

    // ── POST /accounts ──────────────────────────────────────────────────────

    @Test
    fun `POST accounts returns 201 with a uid`() = testApplication {
        setupApp()
        val response = client.post("/accounts")
        assertEquals(HttpStatusCode.Created, response.status)
        val uid = response.bodyAsText().toJsonObject()["uid"]?.jsonPrimitive?.content
        assertNotNull(uid)
        assertNotNull(Uuid.parse(uid))
    }

    @Test
    fun `POST accounts creates distinct accounts on each call`() = testApplication {
        setupApp()
        val uid1 = client.post("/accounts").uid()
        val uid2 = client.post("/accounts").uid()
        assertNotEquals(uid1, uid2)
    }

    // ── PUT /accounts/{accountUid}/transactions/{transactionUid} ─────────────

    @Test
    fun `PUT transaction records a deposit`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        val txUid = Uuid.random()
        val response = putTransaction(accountUid, txUid, TransactionType.DEPOSIT, 1000)
        assertEquals(HttpStatusCode.OK, response.status)
        val tx = response.bodyAsText().toTransactionResponse()
        assertEquals(txUid, tx.uid)
        assertEquals(accountUid, tx.accountUid)
        assertEquals(TransactionType.DEPOSIT, tx.type)
        assertEquals(1000L, tx.amount)
    }

    @Test
    fun `PUT transaction records a withdrawal`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        putTransaction(accountUid, Uuid.random(), TransactionType.DEPOSIT, 1000)
        val response = putTransaction(accountUid, Uuid.random(), TransactionType.WITHDRAWAL, 400)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(TransactionType.WITHDRAWAL, response.bodyAsText().toTransactionResponse().type)
    }

    @Test
    fun `PUT transaction is idempotent — same body returns 200 with original`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        val txUid = Uuid.random()
        putTransaction(accountUid, txUid, TransactionType.DEPOSIT, 1000)
        val response = putTransaction(accountUid, txUid, TransactionType.DEPOSIT, 1000)
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT transaction same uid different amount returns 409`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        val txUid = Uuid.random()
        putTransaction(accountUid, txUid, TransactionType.DEPOSIT, 1000)
        val response = putTransaction(accountUid, txUid, TransactionType.DEPOSIT, 2000)
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `PUT transaction same uid different type returns 409`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        val txUid = Uuid.random()
        putTransaction(accountUid, txUid, TransactionType.DEPOSIT, 1000)
        val response = putTransaction(accountUid, txUid, TransactionType.WITHDRAWAL, 1000)
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `PUT transaction on unknown account returns 404`() = testApplication {
        setupApp()
        val response = putTransaction(Uuid.random(), Uuid.random(), TransactionType.DEPOSIT, 1000)
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT transaction with invalid account uid returns 400`() = testApplication {
        setupApp()
        val response = client.put("/accounts/not-a-uuid/transactions/${Uuid.random()}") {
            contentType(ContentType.Application.Json)
            setBody("""{"type":"DEPOSIT","amount":1000}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT withdrawal exceeding balance returns 422`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        putTransaction(accountUid, Uuid.random(), TransactionType.DEPOSIT, 500)
        val response = putTransaction(accountUid, Uuid.random(), TransactionType.WITHDRAWAL, 501)
        assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `PUT withdrawal exactly equal to balance succeeds`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        putTransaction(accountUid, Uuid.random(), TransactionType.DEPOSIT, 500)
        val response = putTransaction(accountUid, Uuid.random(), TransactionType.WITHDRAWAL, 500)
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // ── GET /accounts/{accountUid}/balance ───────────────────────────────────

    @Test
    fun `GET balance starts at zero`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        val response = client.get("/accounts/$accountUid/balance")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0L, response.balance())
    }

    @Test
    fun `GET balance reflects deposits and withdrawals`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        putTransaction(accountUid, Uuid.random(), TransactionType.DEPOSIT, 1000)
        putTransaction(accountUid, Uuid.random(), TransactionType.DEPOSIT, 500)
        putTransaction(accountUid, Uuid.random(), TransactionType.WITHDRAWAL, 300)
        val response = client.get("/accounts/$accountUid/balance")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1200L, response.balance())
    }

    @Test
    fun `GET balance on unknown account returns 404`() = testApplication {
        setupApp()
        assertEquals(HttpStatusCode.NotFound, client.get("/accounts/${Uuid.random()}/balance").status)
    }

    // ── GET /accounts/{accountUid}/transactions ──────────────────────────────

    @Test
    fun `GET transactions returns empty list for new account`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        val response = client.get("/accounts/$accountUid/transactions")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(0, Json.decodeFromString<List<TransactionResponse>>(response.bodyAsText()).size)
    }

    @Test
    fun `GET transactions returns history in insertion order`() = testApplication {
        setupApp()
        val accountUid = client.post("/accounts").uid()
        val tx1 = Uuid.random()
        val tx2 = Uuid.random()
        putTransaction(accountUid, tx1, TransactionType.DEPOSIT, 1000)
        putTransaction(accountUid, tx2, TransactionType.WITHDRAWAL, 300)
        val response = client.get("/accounts/$accountUid/transactions")
        assertEquals(HttpStatusCode.OK, response.status)
        val txs = Json.decodeFromString<List<TransactionResponse>>(response.bodyAsText())
        assertEquals(2, txs.size)
        assertEquals(tx1, txs[0].uid)
        assertEquals(tx2, txs[1].uid)
    }

    @Test
    fun `GET transactions only returns transactions for the given account`() = testApplication {
        setupApp()
        val acc1 = client.post("/accounts").uid()
        val acc2 = client.post("/accounts").uid()
        val tx1 = Uuid.random()
        putTransaction(acc1, tx1, TransactionType.DEPOSIT, 1000)
        putTransaction(acc2, Uuid.random(), TransactionType.DEPOSIT, 500)
        val txs = Json.decodeFromString<List<TransactionResponse>>(
            client.get("/accounts/$acc1/transactions").bodyAsText()
        )
        assertEquals(1, txs.size)
        assertEquals(tx1, txs[0].uid)
    }

    @Test
    fun `GET transactions on unknown account returns 404`() = testApplication {
        setupApp()
        assertEquals(HttpStatusCode.NotFound, client.get("/accounts/${Uuid.random()}/transactions").status)
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private suspend fun ApplicationTestBuilder.putTransaction(
        accountUid: Uuid,
        txUid: Uuid,
        type: TransactionType,
        amount: Long
    ): HttpResponse = client.put("/accounts/$accountUid/transactions/$txUid") {
        contentType(ContentType.Application.Json)
        setBody("""{"type":"$type","amount":$amount}""")
    }

    private suspend fun HttpResponse.uid(): Uuid =
        Uuid.parse(bodyAsText().toJsonObject()["uid"]!!.jsonPrimitive.content)

    private suspend fun HttpResponse.balance(): Long =
        bodyAsText().toJsonObject()["balance"]!!.jsonPrimitive.long

    private fun String.toJsonObject(): JsonObject = Json.parseToJsonElement(this).jsonObject

    private fun String.toTransactionResponse(): TransactionResponse = Json.decodeFromString(this)
}
