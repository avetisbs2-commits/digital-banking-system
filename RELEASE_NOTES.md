# Digital Banking System v0.1.0

**Release status:** Draft only. This release has not been published or tagged.

Digital Banking System is a Java 17 and Spring Boot learning project that demonstrates account and transaction workflows using PostgreSQL, a Thymeleaf web interface, and Docker.

## Highlights

- Create accounts with generated account numbers and select AMD, USD, or EUR.
- Deposit funds, withdraw with PIN verification, and transfer between accounts.
- Convert currencies using configured rates and review account transaction history.
- View aggregate balances by currency and transfer counts.
- Ask banking-related questions through an AI assistant backed by the OpenAI Responses API.
- Run the application and PostgreSQL with Docker Compose. Adminer is available through the optional `tools` profile.
- Use a GitHub Actions workflow that runs `mvn clean verify`, builds a Docker image, and can trigger a Render deployment on pushes to `main` when `RENDER_DEPLOY_HOOK_URL` is configured.

## Notes and limitations

This is a learning and portfolio project, not production banking software. PIN codes are stored directly in the account entity, the application does not include a full authentication and authorization framework, and currency rates are configured in code rather than fetched from a live provider. Do not use it to store real customer data or process real money.

## Version rationale

`v0.1.0` is proposed as a first pre-1.0 release: the repository contains a coherent working prototype, while its documented security and operational limitations make a stable `v1.0.0` claim premature. The Maven project version is aligned to `0.1.0` for this draft. No Git tag or remote release has been created.

The AI assistant requires `OPENAI_API_KEY` at application startup; the service currently fails to initialize when the key is missing.
