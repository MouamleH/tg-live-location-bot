package team.swiper.location_bot.metrics;

import io.micrometer.influx.InfluxConfig;
import team.swiper.location_bot.configuration.Config;

import java.time.Duration;

@SuppressWarnings("NullableProblems")
public class InfluxMeterConfig implements InfluxConfig {

    @Override
    public String userName() {
        return Config.getString("metrics.influx.username");
    }

    @Override
    public String password() {
        return Config.getString("metrics.influx.password");
    }

    @Override
    public String uri() {
        return Config.getString("metrics.influx.uri");
    }

    @Override
    public String org() {
        return Config.getString("metrics.influx.org");
    }

    @Override
    public String bucket() {
        return Config.getString("metrics.influx.bucket");
    }

    @Override
    public String token() {
        return Config.getString("metrics.influx.token");
    }

    @Override
    public Duration step() {
        return Config.getDuration("metrics.influx.step", Duration.ofSeconds(5));
    }

    @Override
    public String get(String s) {
        return null;
    }

}
