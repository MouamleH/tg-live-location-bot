package team.swiper.location_bot.configuration;

import org.apache.commons.configuration2.convert.DefaultConversionHandler;
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;

import java.time.Duration;

public class CustomConversionHandler extends DefaultConversionHandler {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T to(Object src, Class<T> targetCls, ConfigurationInterpolator ci) {
        if (targetCls == Duration.class) {
            if (src == null) {
                return null;
            }
            final String value = String.valueOf(src);
            final char postfix = value.charAt(value.length() - 1);
            final String timeString = value.substring(0, value.length() - 1);
            return (T) ConversionUtils.convertDuration(postfix, Long.parseLong(timeString));
        }
        return super.to(src, targetCls, ci);
    }

}
