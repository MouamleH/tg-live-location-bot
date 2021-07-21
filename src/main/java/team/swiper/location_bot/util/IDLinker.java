package team.swiper.location_bot.util;

import team.swiper.location_bot.util.cache.SelfExpiringHashMap;
import team.swiper.location_bot.util.cache.SelfExpiringMap;

import java.util.Optional;
import java.util.UUID;

public class IDLinker {

    private static final IDLinker INSTANCE = new IDLinker();

    public static IDLinker getInstance() {
        return INSTANCE;
    }

    private IDLinker() { }

    private final SelfExpiringMap<Integer, String> cache = new SelfExpiringHashMap<>();

    public String linkId(int messageId, int ttlMillis) {
        if (cache.containsKey(messageId)) {
            return cache.get(messageId);
        }

        final String uuid = UUID.randomUUID().toString();
        cache.put(messageId, uuid, ttlMillis);
        return uuid;
    }

    public Optional<String> getId(int messageId) {
        return Optional.ofNullable(cache.get(messageId));
    }

    public void clear(int messageId) {
        cache.remove(messageId);
    }

}
