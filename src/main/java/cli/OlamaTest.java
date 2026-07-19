package cli;

import ai.AIClient;

public class OlamaTest {
    public static void main(String[] args) throws java.io.IOException {
        AIClient aiClient = new AIClient();
        String reply = aiClient.sendPrompt(
                "Respond with ONLY this exact JSON, no other text: {\"message\": \"hello\"}"
        );
        System.out.println(reply);
    }
}