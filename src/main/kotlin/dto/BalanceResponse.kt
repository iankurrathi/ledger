package com.bank.dto

import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponse(val balance: Long)
