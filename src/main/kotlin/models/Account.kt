package com.bank.models

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Account(val uid: Uuid)
