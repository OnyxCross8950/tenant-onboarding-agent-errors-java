package com.example.agenterrors;

public final class TenantOnboardingServiceTest {
  public static void main(String[] args) {
    TenantOnboardingService service = new TenantOnboardingService(null);
    check(service.onboard("t1", true, true) == TenantOnboardingService.Decision.CUTOVER, "ready tenant cuts over");
    check(service.onboard("t2", true, false) == TenantOnboardingService.Decision.ROLLBACK, "unready admin rolls back");
    System.out.println("TenantOnboardingServiceTest passed");
  }
  private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
