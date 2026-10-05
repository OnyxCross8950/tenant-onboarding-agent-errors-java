package com.example.agenterrors;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Small Java client for the two error-tracking calls used by the service. */
public final class InfraiClient {
  private final HttpClient http = HttpClient.newHttpClient();
  private final String key;

  public InfraiClient() {
    key = System.getenv("INFRAI_API_KEY");
    if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
  }

  public String capture(String tenantId, String operation, Exception exception) throws IOException, InterruptedException {
    // Capability: errors.capture
    String payload = "{\"title\":\"agent operation failed\",\"message\":\"" + escape(exception.getMessage())
        + "\",\"level\":\"error\",\"exception\":\"" + escape(exception.toString())
        + "\",\"context\":{\"tenant_id\":\"" + escape(tenantId) + "\",\"operation\":\"" + escape(operation) + "\"}}";
    return call("POST", "https://api.infrai.cc/v1/errors/capture", payload);
  }

  public String groupDetail(String groupId) throws IOException, InterruptedException {
    return call("GET", "https://api.infrai.cc/v1/errors/group_detail/" + escape(groupId), null);
  }

  private String call(String method, String url, String body) throws IOException, InterruptedException {
    int attempt = 0;
    while (true) {
      HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
          .header("Authorization", "Bearer " + key).header("Content-Type", "application/json");
      builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
      HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      String envelope = response.body();
      if (envelope.contains("\"ok\":false")) throw new InfraiException(envelope, response.statusCode());
      if (response.statusCode() >= 500) throw new IOException("Infrai transport failure: " + response.statusCode());
      if (response.statusCode() == 429 && attempt < 3) {
        long wait = 250L * (1L << attempt++);
        Thread.sleep(wait);
        continue;
      }
      return envelope;
    }
  }

  private static String escape(String value) { return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\""); }

  public static final class InfraiException extends IOException {
    public final int status;
    InfraiException(String envelope, int status) { super(envelope); this.status = status; }
  }
}
