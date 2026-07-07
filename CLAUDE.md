# Ledger — Developer Guide

## Project
Tiny ledger REST API built with Kotlin + Ktor. No database — all state is in-memory.

## Build & Run
```bash
./gradlew run                  # start server on :8080
./gradlew test                 # run all tests
./gradlew shadowJar            # build fat jar → build/libs/ledger-all.jar
java -jar build/libs/ledger-all.jar   # run fat jar
```

## Architecture (layered)
```
HTTP layer   →  Routes (Routing.kt)
                  ↓
Business     →  Service (LedgerService.kt)
                  ↓
Storage      →  Repository (LedgerRepository.kt)
                  ↓
Models       →  domain data classes (models/*)
```

Each layer depends only on the layer below it. Routes call Service; Service calls Repository. Tests target each layer in isolation where possible.

## TDD Workflow
1. Write a failing test first.
2. Implement the minimum code to make it pass.
3. Refactor — keep code simple and names clear.
4. Commit after each passing step.

Test files live in `src/test/kotlin/` mirroring the main source layout.

## API surface
| Method | Path                                                    | Description                        | Status codes                          |
|--------|---------------------------------------------------------|------------------------------------|---------------------------------------|
| POST   | /accounts                                               | Create account (no body)           | 201 Created                           |
| PUT    | /accounts/{accountUid}/transactions/{transactionUid}    | Record deposit/withdrawal          | 200 OK, 404, 409 Conflict, 422        |
| GET    | /accounts/{accountUid}/balance                          | Current balance                    | 200 OK, 404                           |
| GET    | /accounts/{accountUid}/transactions                     | Transaction history                | 200 OK, 404                           |

## Assumptions
- Multi-account; each account is created via `POST /accounts` which returns a server-generated UID.
- No authentication or authorisation.
- `amount` is always a positive `Long` (minor currency units, e.g. cents); `type: DEPOSIT | WITHDRAWAL` determines direction.
- Balance cannot go negative — a withdrawal that would overdraw returns 422.
- Timestamps are server-assigned (UTC ISO-8601).
- Data is lost on restart (in-memory as per spec).
- Transaction IDs are client-generated UUIDs — idempotent: same UID + same body → 200 OK, same UID + different body → 409 Conflict.

## Conventions
- Kotlin data classes for all domain models, marked `@Serializable`.
- `LedgerRepository` owns the mutable state (`ConcurrentHashMap` / `CopyOnWriteArrayList`).
- `LedgerService` contains business rules (balance check, overdraft guard) — no HTTP types here.
- Routes are thin: deserialise → call service → serialise response.
- 400 Bad Request for malformed/missing input fields.
