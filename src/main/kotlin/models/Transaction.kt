package com.bank.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Transaction(
    val uid: Uuid,
    val accountUid: Uuid,
    val type: TransactionType,
    val amount: Long,
    val timestamp: Instant
)
