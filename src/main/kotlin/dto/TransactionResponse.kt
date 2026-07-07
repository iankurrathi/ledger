package com.bank.dto

import com.bank.models.Transaction
import com.bank.models.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class TransactionResponse(
    val uid: String,
    val accountUid: String,
    val type: TransactionType,
    val amount: Long,
    val timestamp: String
)

fun Transaction.toResponse() = TransactionResponse(
    uid = uid,
    accountUid = accountUid,
    type = type,
    amount = amount,
    timestamp = timestamp
)
