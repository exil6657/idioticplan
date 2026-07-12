package com.zenith.client.api;

import com.zenith.client.ZenithClient;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal async HTTP client used by all API integrations. Wraps the JDK 11+
 * HttpClient so we don't pull in OkHttp/Apache just for JSON GETs.
 *
 * <p>Every outbound request goes through the shared {@link #io()} client,
 * uses a short timeout (default 8s), and sets a Zenith-User-Agent so endpoints
 * can identify us if they need to (no branding is sent to Hypixel — this is
 * only for Coflnet/NEU/Moulberry, which are third-party public APIs).</p>
 */
public final class HttpClient {

    private static final HttpClient INSTANCE = new HttpClient();

    public static HttpClient getInstance() { return INSTANCE; }

    private final java.net.http.HttpClient client;
    private volatile String userAgent = "ZenithClient/1.0 (+https://github.com/exil6657/idioticplan)";

    private HttpClient() {
        this.client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .executor(com.zenith.client.core.util.ThreadUtils.ioPool())
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();
    }

    public void setUserAgent(String ua) { this.userAgent = ua; }

    /**
     * Asynchronously GET a URL and return the response body as a String.
     * The returned CompletableFuture completes exceptionally on IO/timeout errors.
     */
    public CompletableFuture<String> get(String url) {
        return get(url, Map.of());
    }

    public CompletableFuture<String> get(String url, Map<String, String> headers) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json");
        headers.forEach(b::header);
        HttpRequest req = b.GET().build();
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(resp -> {
                    if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                        throw new RuntimeException("HTTP " + resp.statusCode() + " for " + url);
                    }
                    return resp.body();
                });
    }

    /** Build a URL query string from a map of key/value pairs (UTF-8 encoded). */
    public static String buildQuery(Map<String, String> params) {
        if (params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        for (var e : params.entrySet()) {
            if (!first) sb.append('&');
            first = false;
            sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        return sb.toString();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
