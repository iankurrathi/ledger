package com.bank

import com.bank.models.Account
import com.bank.models.Transaction
import com.bank.models.TransactionType
import java.time.Instant
import java.util.UUID

class LedgerService(private val repository: LedgerRepository) {

    fun createAccount(): Account {
        val account = Account(uid = UUID.randomUUID().toString())
        repository.save(account)
        return account
    }

    sealed class PutTransactionResult {
        data class Success(val transaction: Transaction) : PutTransactionResult()
        object AccountNotFound : PutTransactionResult()
        object Conflict : PutTransactionResult()
        object InsufficientFunds : PutTransactionResult()
    }

    fun putTransaction(
        accountUid: String,
        transactionUid: String,
        type: TransactionType,
        amount: Long
    ): PutTransactionResult {
        repository.findAccount(accountUid) ?: return PutTransactionResult.AccountNotFound

        val existing = repository.findTransaction(transactionUid)
        if (existing != null) {
            return if (existing.type == type && existing.amount == amount)
                PutTransactionResult.Success(existing)
            else
                PutTransactionResult.Conflict
        }

        if (type == TransactionType.WITHDRAWAL) {
            if (amount > balanceFor(accountUid)) return PutTransactionResult.InsufficientFunds
        }

        val tx = Transaction(
            uid = transactionUid,
            accountUid = accountUid,
            type = type,
            amount = amount,
            timestamp = Instant.now().toString()
        )
        repository.save(tx)
        return PutTransactionResult.Success(tx)
    }

    fun getBalance(accountUid: String): Long? {
        repository.findAccount(accountUid) ?: return null
        return balanceFor(accountUid)
    }

    fun getTransactions(accountUid: String): List<Transaction>? {
        repository.findAccount(accountUid) ?: return null
        return repository.findTransactions(accountUid)
    }

    private fun balanceFor(accountUid: String): Long =
        repository.findTransactions(accountUid).sumOf { tx ->
            if (tx.type == TransactionType.DEPOSIT) tx.amount else -tx.amount
        }
}
