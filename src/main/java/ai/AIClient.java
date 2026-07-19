package ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AIClient {

    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Local model inference can be slow depending on hardware — generous timeouts
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();
    private final String model;

    public AIClient() {
        this("llama3.1:8b"); // default — override via constructor if using a different pulled model
    }

    public AIClient(String model) {
        this.model = model;
    }

    public String sendPrompt(String promptText) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.addProperty("prompt", promptText);
        requestBody.addProperty("stream", false);
        requestBody.addProperty("format", "json"); // ask Ollama to enforce JSON output

        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama API call failed: " + response.code() + " " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            return json.get("response").getAsString();
        }
    }
}