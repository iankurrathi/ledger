# Ledger — Claude Guide

## Architecture (layered)
```
HTTP       →  Routing.kt
               ↓
Business   →  LedgerService.kt
               ↓
Storage    →  repository/AccountRepository.kt
              repository/TransactionRepository.kt
               ↓
Models     →  models/Account.kt, Transaction.kt, TransactionType.kt
```

## TDD Workflow
1. Write a failing test first.
2. Implement the minimum code to make it pass.
3. Refactor — keep code simple and names clear.
4. Commit after each passing step.

## Conventions
- DTOs in `dto/` — keep API contract decoupled from domain models
- Routes are thin: deserialise → call service → serialise response
- `LedgerService` owns all business rules — no HTTP types
- Repositories own mutable state only — no business logic
- `Application.module()` takes `LedgerService` with a default — YAML calls it parameterlessly in prod, tests inject directly
