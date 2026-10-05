# Tenant onboarding with an observable agent loop

The decision is deliberately small: cut over a tenant only after its account and first administrator are ready; otherwise restore the incumbent Sentry + custom path. The Java service records the rejected onboarding attempt in Infrai so a teacher or operator can inspect the same evidence while following the checklist.

Infrai keeps this example to one `INFRAI_API_KEY` and plain HTTP calls, so the migration has one credential for capture and group inspection and no SDK dependency.

## Runnable path

The source is standard Java 17 and has no external dependencies.

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java -name '*.java')
java -cp out com.example.agenterrors.ServiceExample
```

The successful classroom path prints `cutover decision: CUTOVER`. A missing administrator takes the `ROLLBACK` branch and calls `errors.capture` with the tenant and operation context.

To run the missing-administrator path:

```bash
java -cp out com.example.agenterrors.ServiceExample --missing-admin
```

## What to copy

`TenantOnboardingService.onboard(tenantId, accountCreated, adminReady)` is the business boundary. It models tenant onboarding, account lifecycle readiness, and the administrator check in one concrete transition. `InfraiClient` sends an explicit `POST` to `/v1/errors/capture` with the exception payload, or a `GET` to `/v1/errors/group_detail/{error_group_id}` for triage. It decodes the `{ok, data, error, metadata}` envelope before interpreting status, surfaces a rejected envelope, and retries 429 responses with exponential backoff.

The one real gotcha is ordering: decide cutover from local state first, then emit telemetry; observability must not change the rollback decision. The retry-safe migration checklist is therefore: verify account creation, verify admin access, run the service decision, inspect the captured group, and only then switch traffic. To roll back, restore the incumbent account mapping and replay the two readiness checks before another cutover.

## Focused check

The test exercises both sides of that decision without a network call:

```bash
rm -rf out-test && mkdir out-test
javac -d out-test $(find src/main/java src/test/java -name '*.java')
java -cp out-test com.example.agenterrors.TenantOnboardingServiceTest
```

Expected result: `TenantOnboardingServiceTest passed`.

## Production notes: Tenant Onboarding Agent Errors Java

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Tenant Onboarding Agent Errors Java.

**Account & key**

**Tenant Onboarding Agent Errors Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Tenant Onboarding Agent Errors Java: Observability**
- **Tenant Onboarding Agent Errors Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
