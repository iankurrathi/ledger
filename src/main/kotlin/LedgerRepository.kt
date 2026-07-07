package com.bank

import com.bank.models.Account
import com.bank.models.Transaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class LedgerRepository {
    private val accounts = ConcurrentHashMap<String, Account>()
    private val transactions = CopyOnWriteArrayList<Transaction>()

    fun save(account: Account) { accounts[account.uid] = account }
    fun findAccount(uid: String): Account? = accounts[uid]

    fun save(transaction: Transaction) { transactions.add(transaction) }
    fun findTransaction(uid: String): Transaction? = transactions.find { it.uid == uid }
    fun findTransactions(accountUid: String): List<Transaction> =
        transactions.filter { it.accountUid == accountUid }
}
