package com.bank

import com.bank.models.Account
import com.bank.models.Transaction
import com.bank.models.TransactionType
import com.bank.repository.AccountRepository
import com.bank.repository.TransactionRepository
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.uuid.Uuid

class LedgerService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {

    private val log = LoggerFactory.getLogger("com.bank.ledger")

    fun createAccount(): Account {
        val account = Account(uid = Uuid.random())
        accountRepository.save(account)
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
        accountUid: Uuid,
        transactionUid: Uuid,
        type: TransactionType,
        amount: Long
    ): PutTransactionResult {
        accountRepository.find(accountUid) ?: run {
            log.warn("transaction.put accountUid=$accountUid txUid=$transactionUid — account not found")
            return PutTransactionResult.AccountNotFound
        }

        val existing = transactionRepository.find(transactionUid)
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
            timestamp = Clock.System.now()
        )
        transactionRepository.save(tx)
        log.info("transaction.put accountUid=$accountUid txUid=$transactionUid type=$type amount=$amount")
        return PutTransactionResult.Success(tx)
    }

    fun getBalance(accountUid: Uuid): Long? {
        accountRepository.find(accountUid) ?: return null
        return balanceFor(accountUid)
    }

    fun getTransactions(accountUid: Uuid): List<Transaction>? {
        accountRepository.find(accountUid) ?: return null
        return transactionRepository.findAll(accountUid)
    }

    private fun balanceFor(accountUid: Uuid): Long =
        transactionRepository.findAll(accountUid).sumOf { tx ->
            if (tx.type == TransactionType.DEPOSIT) tx.amount else -tx.amount
        }
}
