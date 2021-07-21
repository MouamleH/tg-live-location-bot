package team.swiper.location_bot;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.influx.InfluxMeterRegistry;
import me.mouamle.sync.Registry;
import me.mouamle.sync.Server;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import team.swiper.location_bot.handlers.LocationHandler;
import team.swiper.location_bot.metrics.InfluxMeterConfig;
import team.swiper.location_bot.configuration.Config;
import team.swiper.location_bot.metrics.LoggingMeterConfig;
import team.swiper.location_bot.metrics.MetricsCounter;
import team.swiper.location_bot.telegram.LiveLocationBot;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.logging.LogManager;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        initLogging();
        initConfig();
        initMetrics();
        initSocket();
        initTelegramBot();
        logger.info("Application started");
    }

    private static void initLogging() throws IOException {
        final InputStream stream = Application.class.getClassLoader().getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(stream);
    }

    private static void initConfig() throws ConfigurationException {
        final String propertiesFile;
        if (propertiesExist("production.properties")) {
            propertiesFile = "production.properties";
        } else if (propertiesExist("application.properties")) {
            propertiesFile = "production.properties";
        } else {
            throw new IllegalStateException("Could not read application.properties file, please create one in the resources directory");
        }

        Config.init(propertiesFile);
    }

    private static boolean propertiesExist(String fileName) {
        return Application.class.getClassLoader().getResource(fileName) != null;
    }

    private static void initSocket() {
        logger.info("starting web socket server");
        final int port = Config.getInt("socket.port", 1999);
        final Server server = new Server(new InetSocketAddress(port));
        final Thread serverThread = new Thread(server, "socket-server");
        serverThread.start();

        final LocationHandler locationHandler = new LocationHandler();
        Registry.register(locationHandler);

        EventBus.getDefault().register(locationHandler);
        server.getEventBus().register(locationHandler);
        logger.info("started web socket server on port {}", port);
    }

    private static void initMetrics() {
        if (Config.getBoolean("metrics.enabled")) {
            logger.info("enabling metrics");
            EventBus.getDefault().register(new MetricsCounter());

            if (Config.getBoolean("metrics.console.enabled")) {
                Metrics.addRegistry(new LoggingMeterRegistry(new LoggingMeterConfig(), Clock.SYSTEM));
            }

            if (Config.getBoolean("metrics.influx.enabled")) {
                logger.info("enabling influx db metrics on bucket {}", Config.getString("metrics.influx.bucket"));
                Metrics.addRegistry(new InfluxMeterRegistry(new InfluxMeterConfig(), Clock.SYSTEM));
            }
        }
    }

    private static void initTelegramBot() throws TelegramApiException {
        logger.info("starting telegram bot");
        final String token = Config.getString("bot.token", true);
        final String username = Config.getString("bot.username", true);
        final LiveLocationBot liveLocationBot = new LiveLocationBot(token, username);
        final TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(liveLocationBot);
        logger.info("started telegram bot {}", username);
    }

}
