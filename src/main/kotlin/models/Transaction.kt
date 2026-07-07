package com.bank.models

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val uid: String,
    val accountUid: String,
    val type: TransactionType,
    val amount: Long,
    val timestamp: String
)
