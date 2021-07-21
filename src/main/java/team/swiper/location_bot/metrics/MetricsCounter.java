package team.swiper.location_bot.metrics;

import io.micrometer.core.instrument.Metrics;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import team.swiper.location_bot.events.LocationStartEvent;
import team.swiper.location_bot.events.LocationUpdateEvent;

public class MetricsCounter {

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onStartEvent(LocationStartEvent event) {
        Metrics.counter("live_locations_start").increment();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onUpdateEvent(LocationUpdateEvent event) {
        Metrics.counter("live_locations_update").increment();
    }

}
