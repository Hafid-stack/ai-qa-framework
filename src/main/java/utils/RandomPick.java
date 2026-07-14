package utils;

import java.util.List;
import java.util.Random;

public class RandomPick {

    public static final <T> T randomPick(List<T> items) {

        return items.get(new Random().nextInt(items.size()));

    }
}
