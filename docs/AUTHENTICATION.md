# Authentication and Sessions

SocietyHub uses signed access tokens and rotating, server-tracked refresh sessions for every
role application.

## Session lifecycle

```text
email + password
      │
      ▼
POST /api/auth/login
      │
      ├── access token (JWT)
      └── opaque refresh token
              │
              ▼
       stored by role-specific Flutter session manager
              │
      access token expires / API returns 401
              │
              ▼
POST /api/auth/refresh ── rotates refresh token ──> retry original request once
```

The raw refresh token is returned only to the client. PostgreSQL stores its SHA-256 hash.
Every successful refresh revokes the previous token and creates a replacement in the same
session family. Reuse of a replaced token revokes the entire family.

## Backend endpoints

| Method | Path | Authentication | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Public | Verify credentials and start a session |
| `POST` | `/api/auth/refresh` | Refresh token body | Rotate session and issue a new access token |
| `GET` | `/api/auth/me` | Bearer access token | Validate session and return the current active account |
| `POST` | `/api/auth/logout` | Refresh token body | Revoke the current device/browser session |
| `POST` | `/api/auth/logout-all` | Bearer access token | Revoke every refresh session for the account |

Login and refresh responses contain `accessToken`, legacy-compatible `token`,
`refreshToken`, `tokenType`, `expiresIn`, and the user profile/role fields.

For society-scoped accounts, authentication also applies platform-membership policy. Staff and
Member login/refresh are denied when the trial/membership has expired, initial payment is pending,
or a due invoice remains unpaid. Society Admin may still authenticate for payment recovery unless
the society/membership is explicitly suspended or cancelled.

## Flutter behavior

`packages/society_ui` owns the shared `SessionManager` and authenticated `ApiClient`.
Super Admin, Society Admin, Staff, and Member applications all use it.

- Sessions are namespaced by role.
- Startup restoration rotates the stored refresh token and validates its role.
- Multiple simultaneous 401 responses share one in-flight refresh operation.
- The failed request is retried once after refresh.
- Refresh failure clears local state and returns the application to login.
- Logout attempts server revocation and always clears local state, including while offline.
- Legacy access-token-only preference keys are removed.

## Security properties and limitations

- Access tokens include user, role, society, issued-at, and expiry claims.
- Refresh tokens are 384-bit cryptographically random opaque values.
- Only refresh-token hashes are persisted.
- Refresh sessions expire after 30 days by default.
- Disabled or suspended users cannot refresh or query `/me`.
- An authenticated society request is rechecked against current membership data, so an existing
  JWT cannot continue using services after expiry or the invoice due date.
- Missing authentication receives HTTP 401; insufficient role/module receives HTTP 403; a blocked
  membership receives HTTP 402. Society Admin retains profile-read and subscription/payment routes.

For production mobile releases, replace shared-preference token persistence with platform
Keychain/Keystore-backed secure storage. Web applications must use HTTPS with a strict
Content Security Policy and XSS controls.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `JWT_SECRET` | Development-only value | Access-token signing key; minimum 32 bytes |
| `JWT_EXPIRATION` | `900000` | Access-token lifetime in milliseconds (15 minutes) |
| `JWT_REFRESH_EXPIRATION_DAYS` | `30` | Rotating refresh-session lifetime |
