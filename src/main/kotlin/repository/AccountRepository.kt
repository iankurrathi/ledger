package com.bank.repository

import com.bank.models.Account
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

class AccountRepository {
    private val accounts = ConcurrentHashMap<Uuid, Account>()

    fun save(account: Account) { accounts[account.uid] = account }
    fun find(uid: Uuid): Account? = accounts[uid]
}
