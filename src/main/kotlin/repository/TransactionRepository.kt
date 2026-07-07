package com.bank.repository

import com.bank.models.Transaction
import java.util.concurrent.CopyOnWriteArrayList

class TransactionRepository {
    private val transactions = CopyOnWriteArrayList<Transaction>()

    fun save(transaction: Transaction) { transactions.add(transaction) }
    fun find(uid: String): Transaction? = transactions.find { it.uid == uid }
    fun findAll(accountUid: String): List<Transaction> = transactions.filter { it.accountUid == accountUid }
}
