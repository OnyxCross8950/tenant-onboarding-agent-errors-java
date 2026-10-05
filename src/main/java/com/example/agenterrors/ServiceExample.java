package com.example.agenterrors;

/** Runnable entry point for a classroom-sized migration exercise. */
public final class ServiceExample {
  public static void main(String[] args) {
    if (args.length > 1 || (args.length == 1 && !"--missing-admin".equals(args[0]))) {
      throw new IllegalArgumentException("usage: ServiceExample [--missing-admin]");
    }
    TenantOnboardingService service = new TenantOnboardingService(new InfraiClient());
    TenantOnboardingService.Decision decision = service.onboard("tenant-course-17", true, args.length == 0);
    System.out.println("cutover decision: " + decision);
    System.out.println("rollback path: restore incumbent account and replay the checklist");
  }
}
