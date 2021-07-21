package team.swiper.location_bot.metrics;

import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import team.swiper.location_bot.configuration.Config;

import java.time.Duration;

@SuppressWarnings("NullableProblems")
public class LoggingMeterConfig implements LoggingRegistryConfig {

    @Override
    public Duration step() {
        return Config.getDuration("metrics.console.step", Duration.ofMinutes(1));
    }

    @Override
    public String get(String s) {
        return null;
    }

}
