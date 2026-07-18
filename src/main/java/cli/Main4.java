package cli;

import ai.AIClient;

import java.io.IOException;

public class Main4 {
    public static void main(String[] args) {

        AIClient aiClient = new AIClient();
        String reply = null;
        try {
            reply = aiClient.sendPrompt("Say hello in exactly 5 words.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(reply);
}
}
