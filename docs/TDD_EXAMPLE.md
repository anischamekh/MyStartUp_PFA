# TDD example — Auth login (Red → Green → Refactor)

This documents a real test-driven cycle used for `AuthServiceImpl.login`.

## Red phase

A failing test was written first (`AuthServiceImplTest.login_returnsTokens`):

- Expectation: valid credentials return non-null `token` and `refreshToken`, with correct `username` and `role`.
- Initially the service was incomplete or returned null → test failed.

## Green phase

Implementation was added in `AuthServiceImpl`:

- Authenticate via `AuthenticationManager`.
- Load the user entity from `UserRepository`.
- Build JWT access and refresh tokens with `SharedJwtService`.
- Return `LoginResponse` with all required fields.

The test passed after wiring mocks for `authenticationManager`, `userRepository`, and token services.

## Refactor phase

Without changing behaviour:

- Extracted token claim building and refresh-token persistence into `RefreshTokenService`.
- Centralised JWT signing in `common-lib` (`SharedJwtService`) so gateway and microservices share the same algorithm.
- Kept the test as a regression guard; no “fake” assertions.

See `microservices/auth-service/src/test/java/tn/iteam/backend/service/impl/AuthServiceImplTest.java`.
