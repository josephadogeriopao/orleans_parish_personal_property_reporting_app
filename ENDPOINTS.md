# Tax Filing Application Backend - API Specification & Migration Guide

This repository contains the backend architecture and REST API endpoint specification for the Tax Filing Application. This system has been migrated from a legacy stateful Jakarta Server Faces (JSF) application into a modern, stateless Spring Boot 3.x microservice using an Access + Refresh JWT Token authentication scheme.

---

## 1. Architectural Strategy: Transitioning to Stateless REST APIs

To achieve horizontal scalability and enhanced production security, the legacy stateful session architecture has been replaced with a stateless architectural design pattern:

1. **Eliminated `@SessionScoped` Memory State:** 
   Legacy session variables such as `private User currentUser` and `private Form currentForm` held in server web container memory have been removed. The backend no longer tracks caller state across independent web requests.
2. **Dynamic Context Extraction:**
   Every secure endpoint extracts user context dynamically at runtime. The user's underlying identity parameters (e.g., `userId`, `username`, `roles`) are extracted from the decrypted **Access Token** passed inside the incoming HTTP request's `Authorization: Bearer <JWT>` header.
3. **Explicit Context Parameter Passing:**
   State tracking structures like `setCurrentForm(Form)` have been decommissioned. APIs now explicitly accept path variable resource identifiers (e.g., `/api/forms/{formId}/business-info`). The system loads the record matching the variable parameter, enforces resource tenant validation, applies modifications, and commits the state to persistence in a single thread transaction execution loop.

---

## 2. Grouped API Endpoint Resource Matrix

### 🔐 Auth & Identity Resource (`/api/auth`)
*Endpoints that handle registration, credential verification, and account validation.*

* **POST `/api/auth/register`**
  * **Legacy Method Mapping:** `createUserAccount(...)`
  * **Description:** Registers a new self-service user profile with status `DISABLED` and generates a temporary `UserChange` uuid validation link token.
* **POST `/api/auth/verify-email`**
  * **Legacy Method Mapping:** `verifyEmailAddress(...)`
  * **Description:** Evaluates registration verification tokens and transitions client account status parameters to `ENABLED`.
* **POST `/api/auth/resend-verification`**
  * **Legacy Method Mapping:** `resendVerificationEmail(...)`
  * **Description:** Clears outstanding stale activation token tracking records, generates a new system verification token, and triggers a fresh outbound confirmation email.
* **POST `/api/auth/login`**
  * **Legacy Method Mapping:** `login(...)`
  * **Description:** Authenticates login credentials. On success, resets tracking lockout metrics and returns signed **Access and Refresh Tokens**. On failure, increments the login failure counter and locks the profile if thresholds are exceeded.
* **POST `/api/auth/forgot-password`**
  * **Legacy Method Mapping:** `submitResetPasswordRequest(...)`
  * **Description:** Triggers user password recovery workflows by storing trackable verification tokens and dispatching security alert notifications.
* **POST `/api/auth/reset-password`**
  * **Legacy Method Mapping:** `resetPassword(...)`
  * **Description:** Overwrites credentials securely when provided an authentic, non-expired out-of-band account restoration token.

### 👤 Profile Management Resource (`/api/profile`)
*Endpoints operating explicitly on the currently authenticated user session context.*

* **PUT `/api/profile`**
  * **Legacy Method Mapping:** `updateProfile(...)`
  * **Description:** Modifies target profile metadata properties (username, full name, phone number) for the authenticated caller.
* **PATCH `/api/profile/password`**
  * **Legacy Method Mapping:** `resetProfilePassword(...)`
  * **Description:** Alters backend access passwords directly from an authorized user profile configuration dashboard view.
* **PATCH `/api/profile/phone`**
  * **Legacy Method Mapping:** `resetPhoneNumber(...)`
  * **Description:** Updates the primary telephone contact metadata linked with the active account profile.

### 📋 Tax Forms Resource (`/api/forms`)
*Endpoints managing the filing state machine, business entity declarations, and specific accounting data structures.*

* **GET `/api/forms`**
  * **Legacy Method Mapping:** `getForms()`
  * **Description:** Returns all tax filing instances explicitly owned by the authorized user token, sorted descending by calendar year and internal status codes.
* **POST `/api/forms/claim-lat5`**
  * **Legacy Method Mapping:** `fileNewLAT5(...)`
  * **Description:** Scans database records for an unassigned default form structure matching physical coordinates (`billNumber` and `PIN`) and couples it to the active profile account.
* **GET `/api/forms/{formId}`**
  * **Legacy Method Mapping:** `getForm(...)` / `getCurrentForm()`
  * **Description:** Resolves and returns full data models for a distinct filing instance after checking data ownership access controls.
* **PUT `/api/forms/{formId}/business-info`**
  * **Legacy Method Mapping:** `storeBusinessInfo(...)`
  * **Description:** Commits enterprise business configuration profiles and sets the target document processing status to `IN_PROGRESS`.
* **POST `/api/forms/{formId}/inventories`**
  * **Legacy Method Mapping:** `storeInventories(...)`
  * **Description:** Attaches batch physical inventory item sheets and commercial asset catalog lists to the parent tax document.
* **POST `/api/forms/{formId}/filings`**
  * **Legacy Method Mapping:** `storeFilings(...)`
  * **Description:** Records individual line item acquisition costs and asset documentation logs within a targeted document block.
* **DELETE `/api/forms/{formId}/filings`**
  * **Legacy Method Mapping:** `deleteFilings(...)`
  * **Description:** Deletes a batch of lines permanently via primary identifier arrays and reverts the parent file state back to `IN_PROGRESS`.
* **POST `/api/forms/{formId}/submit`**
  * **Legacy Method Mapping:** `submitCurrentForm()`
  * **Description:** Validates form structures, flags document timelines permanently as `SUBMITTED`, records validation metrics, and fires electronic compliance alerts.
* **POST `/api/forms/{formId}/unsubmit`**
  * **Legacy Method Mapping:** `unsubmitCurrentForm()`
  * **Description:** Rolls back form records from a locked state back into an editable `IN_PROGRESS` layout.

### 🛠️ Administrative Management Resource (`/api/admin`)
*Privileged backend processing channels restricted exclusively to system accounts with higher structural authorities.*

* **POST `/api/admin/users/register`**
  * **Legacy Method Mapping:** `adminCreateUserAccount(...)`
  * **Description:** Bypasses self-service verification workflows to force-create fully operational system user profiles with pre-assigned permissions (e.g., `TAX_PREPARER`).
* **GET `/api/admin/forms/search`**
  * **Legacy Method Mapping:** `getAllForms(...)`
  * **Description:** Performs global administrative data lookups to return every tax form created across the system for a given filing year.
* **POST `/api/admin/users/{userId}/enable`**
  * **Legacy Method Mapping:** `adminEnableUserAccount(...)`
  * **Description:** Explicitly transitions manual profiles back to active running states or releases administrative login freezes.
* **POST `/api/admin/users/{userId}/lock`**
  * **Legacy Method Mapping:** `adminLockUserAccount(...)`
  * **Description:** Suspends interface navigation access rights immediately for a chosen user profile, blocking active API authentication passes.
* **POST `/api/admin/users/{userId}/disable`**
  * **Legacy Method Mapping:** `adminDisableUserAccount(...)`
  * **Description:** Deactivates user account credentials and flags profile parameters as disabled.
* **POST `/api/admin/system/cache-evict`**
  * **Legacy Method Mapping:** `flushEntityManagerCache()`
  * **Description:** Evicts data elements from secondary shared persistence layout levels to force data re-synchronization.

---

## 3. Reference Endpoint Specification Chart

The following chart contains the baseline mappings for all system endpoints.

| HTTP Method | Endpoint Path | Resource Group | Legacy Java Method | Access Control Level | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/register` | Auth & Identity | `createUserAccount` | Public | Registers a new user account with `DISABLED` status and creates a verification token. |
| **POST** | `/api/auth/verify-email` | Auth & Identity | `verifyEmailAddress` | Public | Validates the registration UUID token and credentials to switch user status to `ENABLED`. |
| **POST** | `/api/auth/resend-verification` | Auth & Identity | `resendVerificationEmail` | Public | Clears old verification details and creates a new tracking record before sending an email. |
| **POST** | `/api/auth/login` | Auth & Identity | `login` | Public | Checks credentials. Increments failures to lock accounts if thresholds break, or provides JWTs on success. |
| **POST** | `/api/auth/forgot-password` | Auth & Identity | `submitResetPasswordRequest` | Public | Processes user password recovery requests and dispatches validation notifications. |
| **POST** | `/api/auth/reset-password` | Auth & Identity | `resetPassword` | Public | Updates account passwords when supplied with a valid registration token string. |
| **PUT** | `/api/profile` | Profile Management | `updateProfile` | Authenticated User | Modifies personal attributes like username, name, and phone details for the caller. |
| **PATCH** | `/api/profile/password` | Profile Management | `resetProfilePassword` | Authenticated User | Changes application passwords from an authorized user profile control window. |
| **PATCH** | `/api/profile/phone` | Profile Management | `resetPhoneNumber` | Authenticated User | Updates primary phone tracking details for the currently logged-in account. |
| **POST** | `/api/forms/{formId}/unsubmit` | Tax Forms | `unsubmitCurrentForm` | Authenticated User | Rolls back active tracking flags from locked status codes to a modifiable `IN_PROGRESS` setup. |
| **POST** | `/api/admin/users/register` | Admin Management | `adminCreateUserAccount` | Admin Only | Allows administrators to directly spin up accounts with custom access roles. |
| **GET** | `/api/admin/forms/search` | Admin Management | `getAllForms` | Admin Only | Returns a global list of every tax form created in the system, filtered by filing year. |
| **POST** | `/api/admin/users/{userId}/enable` | Admin Management | `adminEnableUserAccount` | Admin Only | Force-enables a specified user profile or releases administrative login freezes. |
| **POST** | `/api/admin/users/{userId}/lock` | Admin Management | `adminLockUserAccount` | Admin Only | Restricts profile access instantly by marking an account status as `LOCKED`. |
| **POST** | `/api/admin/users/{userId}/disable` | Admin Management | `adminDisableUserAccount` | Admin Only | Deactivates matching user credentials and suspends access rights across endpoints. |
| **POST** | `/api/admin/system/cache-evict` | Admin Management | `flushEntityManagerCache` | Admin Only | Clears shared internal database persistence caches to ensure data consistency. |
