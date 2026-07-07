package com.bank

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ServerTest {

    @Test
    fun `POST accounts endpoint is reachable`() = testApplication {
        application {
            attributes.put(LedgerServiceKey, LedgerService(LedgerRepository()))
            configureSerialization()
            configureRouting()
        }
        assertEquals(HttpStatusCode.Created, client.post("/accounts").status)
    }
}
