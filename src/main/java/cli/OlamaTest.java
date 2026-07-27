package cli;

import ai.OllamaClient;

public class OlamaTest {
    public static void main(String[] args) throws java.io.IOException {
        OllamaClient ollamaClient = new OllamaClient();
        String reply = ollamaClient.sendPrompt(
                "Respond with ONLY this exact JSON, no other text: {\"message\": \"hello\"}"
        );
        System.out.println(reply);
    }
}