package ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class GeminiClient implements AIProvider {

    private static final String MODEL = "gemini-3.1-flash-lite";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Retry policy for transient server-side failures. Google returns 503 UNAVAILABLE when the
    // model is temporarily overloaded and 429 when rate-limited; both are recoverable by waiting.
    // Without retries a single spike aborts a whole experimental run - including the manual
    // login step - which made measurement runs unreliable.
    private static final int MAX_ATTEMPTS = 4;
    private static final long[] BACKOFF_MS = {2000L, 5000L, 15000L};
    private static final java.util.Set<Integer> RETRYABLE_CODES = java.util.Set.of(429, 500, 502, 503, 504);

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            // Pipeline B sends a whole page of HTML and asks for a full Java class back;
            // 30s was not always enough for that generation to finish.
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();
    private final String apiKey;

    public GeminiClient() {
        this.apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not set");
        }
    }

    public String sendPrompt(String promptText) throws IOException {
        JsonObject requestBody = new JsonObject();

        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", promptText);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);

        requestBody.add("contents", contents);

        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("x-goog-api-key", apiKey)
                .post(body)
                .build();

        IOException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return executeOnce(request);
            } catch (RetryableApiException e) {
                lastFailure = new IOException(e.getMessage());
                if (attempt == MAX_ATTEMPTS) break;
                long backoffMs = BACKOFF_MS[attempt - 1];
                System.out.println("[GEMINI-RETRY] attempt " + attempt + "/" + MAX_ATTEMPTS
                        + " failed: " + e.getMessage() + " - retrying in " + backoffMs + " ms");
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new IOException("Gemini API call failed after " + MAX_ATTEMPTS + " attempts. Last error: "
                + (lastFailure == null ? "unknown" : lastFailure.getMessage()));
    }

    // Marks a failure the caller should wait and retry, as opposed to a permanent error
    // (bad API key, malformed request) where retrying would be pointless.
    private static class RetryableApiException extends Exception {
        RetryableApiException(String message) { super(message); }
    }

    private static String shortened(String text) {
        String oneLine = text.replaceAll("\\s+", " ").trim();
        return oneLine.length() > 160 ? oneLine.substring(0, 160) + "..." : oneLine;
    }

    private String executeOnce(Request request) throws IOException, RetryableApiException {
        long startMs = System.currentTimeMillis();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                if (RETRYABLE_CODES.contains(response.code())) {
                    throw new RetryableApiException("HTTP " + response.code() + " " + shortened(errorBody));
                }
                throw new IOException("Gemini API call failed: " + response.code() + " " + errorBody);
            }

            String responseBody = response.body().string();
            long elapsedMs = System.currentTimeMillis() - startMs;
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);

            // Telemetry only (PFE evaluation): records the token cost and latency of every
            // Gemini call so Pipeline A and Pipeline B can be compared on real measured cost
            // instead of an estimate. Purely additive - does not affect the returned text.
            if (json.has("usageMetadata")) {
                JsonObject usage = json.getAsJsonObject("usageMetadata");
                int promptTokens = usage.has("promptTokenCount") ? usage.get("promptTokenCount").getAsInt() : 0;
                int outputTokens = usage.has("candidatesTokenCount") ? usage.get("candidatesTokenCount").getAsInt() : 0;
                int totalTokens = usage.has("totalTokenCount") ? usage.get("totalTokenCount").getAsInt() : 0;
                System.out.println("[GEMINI-METRICS] model=" + MODEL
                        + " promptTokens=" + promptTokens
                        + " outputTokens=" + outputTokens
                        + " totalTokens=" + totalTokens
                        + " elapsedMs=" + elapsedMs);
            } else {
                System.out.println("[GEMINI-METRICS] model=" + MODEL
                        + " elapsedMs=" + elapsedMs + " (no usageMetadata in response)");
            }

            return json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        }
    }
}