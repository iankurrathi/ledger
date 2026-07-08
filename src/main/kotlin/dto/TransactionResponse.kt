package com.bank.dto

import com.bank.models.Transaction
import com.bank.models.TransactionType
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class TransactionResponse(
    val uid: Uuid,
    val accountUid: Uuid,
    val type: TransactionType,
    val amount: Long,
    val timestamp: Instant
)

fun Transaction.toResponse() = TransactionResponse(
    uid = uid,
    accountUid = accountUid,
    type = type,
    amount = amount,
    timestamp = timestamp
)
