package com.bank.dto

import com.bank.models.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(val type: TransactionType, val amount: Long)
