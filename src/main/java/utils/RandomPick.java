package utils;

import java.util.List;
import java.util.Random;

public class RandomPick {

    public static final <T> T randomPick(List<T> items) {

        if (items.isEmpty()) {
            throw new IllegalStateException("randomPick called on an empty list - nothing was found on the page to pick from");
        }
        return items.get(new Random().nextInt(items.size()));

    }
}
