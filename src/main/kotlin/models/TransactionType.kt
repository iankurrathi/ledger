package com.bank.models

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType { DEPOSIT, WITHDRAWAL }
