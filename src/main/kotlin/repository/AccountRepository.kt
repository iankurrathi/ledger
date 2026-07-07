package com.bank.repository

import com.bank.models.Account
import java.util.concurrent.ConcurrentHashMap

class AccountRepository {
    private val accounts = ConcurrentHashMap<String, Account>()

    fun save(account: Account) { accounts[account.uid] = account }
    fun find(uid: String): Account? = accounts[uid]
}
