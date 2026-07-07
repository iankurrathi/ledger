# Ledger

A simple in-memory ledger REST API built with Kotlin + Ktor.

## Run

```bash
./gradlew run
```

Server starts on `http://localhost:8080`.

## API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/accounts` | Create account |
| `PUT` | `/accounts/{accountUid}/transactions/{transactionUid}` | Record deposit or withdrawal |
| `GET` | `/accounts/{accountUid}/balance` | Current balance |
| `GET` | `/accounts/{accountUid}/transactions` | Transaction history |

## Examples

**Create an account**
```bash
curl -X POST http://localhost:8080/accounts
# {"uid":"e3b0c442-..."}
```

**Record a deposit**
```bash
curl -X PUT http://localhost:8080/accounts/{accountUid}/transactions/{transactionUid} \
  -H "Content-Type: application/json" \
  -d '{"type":"DEPOSIT","amount":1000}'
```

**Record a withdrawal**
```bash
curl -X PUT http://localhost:8080/accounts/{accountUid}/transactions/{transactionUid} \
  -H "Content-Type: application/json" \
  -d '{"type":"WITHDRAWAL","amount":500}'
```

**Get balance**
```bash
curl http://localhost:8080/accounts/{accountUid}/balance
# {"balance":500}
```

**Get transaction history**
```bash
curl http://localhost:8080/accounts/{accountUid}/transactions
```

## Notes

- `amount` is a positive `Long` (minor currency units, e.g. pence/cents)
- Transaction UIDs are client-generated — PUT is idempotent; same UID + same body returns the existing transaction, same UID + different body returns 409
- Withdrawals that exceed the balance return 422
- Data is in-memory and resets on restart

## Test

```bash
./gradlew test
```
