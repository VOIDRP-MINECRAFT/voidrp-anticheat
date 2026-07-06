package ru.voidrp.anticheat.backend;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.voidrp.anticheat.VoidRpAnticheat;
import ru.voidrp.anticheat.config.AnticheatConfig;
import ru.voidrp.anticheat.config.RemoteConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackendClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private final HttpClient http;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "anticheat-http");
        t.setDaemon(true);
        return t;
    });

    public BackendClient() {
        this.http = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    }

    public void reportViolation(String uuid, String nick, String checkType,
                                 String details, double actualValue, double expectedMax,
                                 int vl, String severity) {
        String secret = AnticheatConfig.GAME_AUTH_SECRET.get();
        if (secret == null || secret.isBlank()) return;

        String body = String.format(
                "{\"player_uuid\":\"%s\",\"player_nick\":\"%s\",\"check_type\":\"%s\"," +
                "\"details\":\"%s\",\"actual_value\":%.3f,\"expected_max\":%.3f," +
                "\"vl\":%d,\"severity\":\"%s\"}",
                uuid, escapeJson(nick), checkType, escapeJson(details),
                actualValue, expectedMax, vl, severity
        );
        postAsync("/api/v1/anticheat/violation", body, secret);
    }

    public void reportInjection(String uuid, String nick,
                                 List<String> javaAgents, List<String> suspiciousLibraries,
                                 boolean agentsDetected) {
        String secret = AnticheatConfig.GAME_AUTH_SECRET.get();
        if (secret == null || secret.isBlank()) return;

        String agentsJson  = toJsonArray(javaAgents);
        String libsJson    = toJsonArray(suspiciousLibraries);
        String body = String.format(
                "{\"player_uuid\":\"%s\",\"player_nick\":\"%s\"," +
                "\"java_agents\":%s,\"suspicious_libraries\":%s,\"agents_detected\":%b}",
                uuid, escapeJson(nick), agentsJson, libsJson, agentsDetected
        );
        postAsync("/api/v1/anticheat/injection-report", body, secret);
    }

    public void reportModSnapshot(String uuid, String nick, List<String> mods) {
        String secret = AnticheatConfig.GAME_AUTH_SECRET.get();
        if (secret == null || secret.isBlank()) return;

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < mods.size(); i++) {
            sb.append('"').append(escapeJson(mods.get(i))).append('"');
            if (i < mods.size() - 1) sb.append(',');
        }
        sb.append(']');

        String body = String.format(
                "{\"player_uuid\":\"%s\",\"player_nick\":\"%s\",\"mods\":%s}",
                uuid, escapeJson(nick), sb
        );
        postAsync("/api/v1/anticheat/mod-snapshot", body, secret);
    }

    public void fetchConfig() {
        String secret = AnticheatConfig.GAME_AUTH_SECRET.get();
        if (secret == null || secret.isBlank()) return;
        String baseUrl = AnticheatConfig.BACKEND_URL.get();
        executor.submit(() -> {
            try {
                HttpRequest.Builder rb = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/api/v1/anticheat/config"))
                        .header("X-Game-Auth-Secret", secret)
                        .timeout(TIMEOUT)
                        .GET();
                applyServerSlug(rb);
                HttpRequest req = rb.build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    Map<String, Double> parsed = parseConfigJson(resp.body());
                    RemoteConfigManager.onConfigLoaded(parsed);
                    VoidRpAnticheat.LOG.info("[AntiCheat] Remote config loaded: {}", parsed);
                } else {
                    VoidRpAnticheat.LOG.warn("[AntiCheat] Config fetch returned {}", resp.statusCode());
                }
            } catch (Exception e) {
                VoidRpAnticheat.LOG.warn("[AntiCheat] Config fetch error: {}", e.getMessage());
            }
        });
    }

    private static Map<String, Double> parseConfigJson(String json) {
        Map<String, Double> result = new HashMap<>();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getAsDouble());
            }
        } catch (Exception e) {
            VoidRpAnticheat.LOG.warn("[AntiCheat] Failed to parse config JSON: {}", e.getMessage());
        }
        return result;
    }

    /** Adds the optional X-Server-Slug header for explicit multi-server attribution. */
    private static void applyServerSlug(HttpRequest.Builder rb) {
        String slug = AnticheatConfig.SERVER_SLUG.get();
        if (slug != null && !slug.isBlank()) {
            rb.header("X-Server-Slug", slug);
        }
    }

    private void postAsync(String path, String body, String secret) {
        String baseUrl = AnticheatConfig.BACKEND_URL.get();
        executor.submit(() -> {
            try {
                HttpRequest.Builder rb = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + path))
                        .header("Content-Type", "application/json")
                        .header("X-Game-Auth-Secret", secret)
                        .timeout(TIMEOUT)
                        .POST(HttpRequest.BodyPublishers.ofString(body));
                applyServerSlug(rb);
                HttpRequest req = rb.build();
                HttpResponse<Void> resp = http.send(req, HttpResponse.BodyHandlers.discarding());
                if (resp.statusCode() >= 400) {
                    VoidRpAnticheat.LOG.warn("[AntiCheat] Backend {} returned {}", path, resp.statusCode());
                }
            } catch (Exception e) {
                VoidRpAnticheat.LOG.warn("[AntiCheat] Backend error {}: {}", path, e.getMessage());
            }
        });
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append('"').append(escapeJson(list.get(i))).append('"');
            if (i < list.size() - 1) sb.append(',');
        }
        sb.append(']');
        return sb.toString();
    }
}
