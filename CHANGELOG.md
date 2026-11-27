# Changelog

All notable project changes are recorded here. Entries are based on the repository history and current implementation.

## Draft for v0.1.0 (not published)

### Added
- Account creation with generated account numbers, PIN validation, and AMD, USD, or EUR balances.
- Deposits, PIN-checked withdrawals, account-to-account transfers, and transaction statements.
- Configured currency conversion between AMD, USD, and EUR, plus aggregate balance and transfer-count analytics.
- An AI Banking Assistant endpoint backed by the OpenAI Responses API.
- PostgreSQL persistence with Spring Data JPA and transaction-history indexes for sender and receiver account lookups.
- Docker and Docker Compose configuration for the application and PostgreSQL, with optional Adminer.
- A GitHub Actions workflow to run Maven verification, build a Docker image, and trigger a Render deployment on pushes to `main` when the required secret is configured.
- Project documentation and a redesigned banking dashboard.

### Changed
- Aligned the Maven project version with the proposed `0.1.0` release draft.
- Renamed the domain models to `AccountEntity` and `TransactionEntity`.
- Updated PostgreSQL configuration for deployment and environment-based settings.

### Notes
- This is a proposed initial pre-1.0 release based on history from 2026-10-19 through 2026-11-23. No Git tag or published release was found during preparation.
- This project is a learning prototype. It stores PIN codes directly and does not include an authentication and authorization framework; it is not suitable for real financial transactions.
