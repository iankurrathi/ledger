package com.bank

import com.bank.models.Account
import com.bank.models.Transaction
import com.bank.models.TransactionType
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

class LedgerService(private val repository: LedgerRepository) {

    private val log = LoggerFactory.getLogger("com.bank.ledger")

    fun createAccount(): Account {
        val account = Account(uid = UUID.randomUUID().toString())
        repository.save(account)
        log.info("account.create uid=${account.uid}")
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
        repository.findAccount(accountUid) ?: run {
            log.warn("transaction.put accountUid=$accountUid txUid=$transactionUid — account not found")
            return PutTransactionResult.AccountNotFound
        }

        val existing = repository.findTransaction(transactionUid)
        if (existing != null) {
            return if (existing.type == type && existing.amount == amount) {
                log.info("transaction.put accountUid=$accountUid txUid=$transactionUid — idempotent repeat")
                PutTransactionResult.Success(existing)
            } else {
                log.warn("transaction.put accountUid=$accountUid txUid=$transactionUid — conflict with existing transaction")
                PutTransactionResult.Conflict
            }
        }

        if (type == TransactionType.WITHDRAWAL) {
            val balance = balanceFor(accountUid)
            if (amount > balance) {
                log.warn("transaction.put accountUid=$accountUid txUid=$transactionUid — insufficient funds amount=$amount balance=$balance")
                return PutTransactionResult.InsufficientFunds
            }
        }

        val tx = Transaction(
            uid = transactionUid,
            accountUid = accountUid,
            type = type,
            amount = amount,
            timestamp = Instant.now().toString()
        )
        repository.save(tx)
        log.info("transaction.put accountUid=$accountUid txUid=$transactionUid type=$type amount=$amount")
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
