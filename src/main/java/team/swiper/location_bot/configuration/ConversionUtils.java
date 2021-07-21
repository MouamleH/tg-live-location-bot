package team.swiper.location_bot.configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConversionUtils {

    private static final Map<Character, Function<Long, Duration>> durationConvertors = new HashMap<>();

    static {
        durationConvertors.put('s', Duration::ofSeconds);
        durationConvertors.put('m', Duration::ofMinutes);
        durationConvertors.put('h', Duration::ofHours);
        durationConvertors.put('d', Duration::ofDays);
    }

    public static Duration convertDuration(char postfix, long value) {

        return durationConvertors.get(postfix).apply(value);
    }

}
