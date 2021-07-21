package team.swiper.location_bot.util.cache;

import java.util.Map;

public interface SelfExpiringMap<K, V> extends Map<K, V> {

    boolean renewKey(K key);

    V put(K key, V value, long lifeTimeMillis);

}