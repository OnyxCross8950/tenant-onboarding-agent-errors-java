package com.example.agenterrors;

/** Domain workflow: provision a tenant, then keep the incumbent rollback path explicit. */
public final class TenantOnboardingService {
  public enum Decision { CUTOVER, ROLLBACK }
  private final InfraiClient infrai;

  public TenantOnboardingService(InfraiClient infrai) { this.infrai = infrai; }

  public Decision onboard(String tenantId, boolean accountCreated, boolean adminReady) {
    if (accountCreated && adminReady) return Decision.CUTOVER;
    try {
      infrai.capture(tenantId, "tenant-onboarding", new IllegalStateException("onboarding checks incomplete"));
    } catch (Exception ignored) {
      // The migration decision remains deterministic even when telemetry is unavailable.
    }
    return Decision.ROLLBACK;
  }
}
