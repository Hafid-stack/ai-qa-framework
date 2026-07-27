package ai;

import java.io.IOException;

public interface AIProvider {
    String sendPrompt(String promptText) throws IOException;
}