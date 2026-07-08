package com.bank.repository

import com.bank.models.Transaction
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.uuid.Uuid

class TransactionRepository {
    private val transactions = CopyOnWriteArrayList<Transaction>()

    fun save(transaction: Transaction) { transactions.add(transaction) }
    fun find(uid: Uuid): Transaction? = transactions.find { it.uid == uid }
    fun findAll(accountUid: Uuid): List<Transaction> = transactions.filter { it.accountUid == accountUid }
}
