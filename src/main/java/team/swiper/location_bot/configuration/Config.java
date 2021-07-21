package team.swiper.location_bot.configuration;

import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.time.Duration;

public class Config {

    private static FileBasedConfiguration configuration;

    public static void init(String filename) throws ConfigurationException {
        final PropertiesBuilderParameters properties = new Parameters()
                .properties()
                .setFileName(filename)
                .setConversionHandler(new CustomConversionHandler());

        configuration = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(properties)
                .getConfiguration();
    }

    public static Duration getDuration(String key, Duration defaultValue) {
        return configuration.get(Duration.class, key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return configuration.getInt(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return configuration.getBoolean(key);
    }

    public static String getString(String key) {
        return getString(key, false);
    }

    public static String getString(String key, boolean validate) {
        final String value = configuration.getString(key);
        if (validate && (value == null || value.isEmpty())) {
            throw new IllegalArgumentException(String.format("property %s must be set in application.properties", key));
        }
        return value;
    }

}
