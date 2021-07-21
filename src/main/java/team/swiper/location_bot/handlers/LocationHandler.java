package team.swiper.location_bot.handlers;

import io.micrometer.core.instrument.Metrics;
import me.mouamle.sync.event.OnClose;
import me.mouamle.sync.event.OnError;
import me.mouamle.sync.packet.Packet;
import me.mouamle.sync.packet.handler.Handler;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Location;
import team.swiper.location_bot.events.InfoEvent;
import team.swiper.location_bot.events.LocationStartEvent;
import team.swiper.location_bot.events.LocationStopEvent;
import team.swiper.location_bot.events.LocationUpdateEvent;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocationHandler extends Handler<String, Location> {
    private static final Logger logger = LoggerFactory.getLogger(LocationHandler.class);

    private final Map<String, Queue<WebSocket>> subscribers = new ConcurrentHashMap<>();
    private final Map<String, LocationStartEvent> queuedEvents = new ConcurrentHashMap<>();

    public LocationHandler() {
        Metrics.gauge("live_locations", subscribers.keySet(), Set::size);
        Metrics.gauge("subscribers", subscribers.values(), values -> {
            int total = 0;
            for (Queue<WebSocket> queue : values) {
                total += queue.size();
            }
            return total;
        });
    }

    @Override
    public void handle(WebSocket webSocket, String id) {
        final Queue<WebSocket> sockets = subscribers.getOrDefault(id, new ArrayDeque<>());
        sockets.add(webSocket);
        subscribers.put(id, sockets);
        final LocationStartEvent event = queuedEvents.get(id);

        if (event == null) {
            reply(webSocket, "info", new InfoEvent("invalid uuid"));
            return;
        }

        reply(webSocket, "start", event);
        logger.info("sending subscribe-start event");
    }

    @Subscribe
    public void onClose(OnClose event) {
        for (Queue<WebSocket> sockets : subscribers.values()) {
            sockets.remove(event.getSocket());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onError(OnError event) {
        for (Queue<WebSocket> sockets : subscribers.values()) {
            sockets.remove(event.getSocket());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onStartEvent(LocationStartEvent event) {
        queuedEvents.put(event.getUuid(), event);
        reply(event.getUuid(), "start", event);
        logger.info("sending start event");
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onUpdateEvent(LocationUpdateEvent event) {
        reply(event.getUuid(), "update", event);
        logger.info("sending update event");
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onStopEvent(LocationStopEvent event) {
        reply(event.getUuid(), "stop", event);
        subscribers.remove(event.getUuid());
        queuedEvents.remove(event.getUuid());
        logger.info("sending stop event");
    }

    private void reply(String eventUUID, String type, Object data) {
        final Queue<WebSocket> sockets = subscribers.getOrDefault(eventUUID, new ArrayDeque<>());
        logger.info("Sending to {} users", sockets.size());
        for (WebSocket socket : sockets) {
            logger.info("Sending event type {} with data {}", type, data);
            reply(socket, type, data);
        }
    }

    private void reply(WebSocket socket, String type, Object data) {
        socket.send(this.gson.toJson(new Packet(type, data)));
    }

    @Override
    public Class<String> getPayloadType() {
        return String.class;
    }

    @Override
    public Class<Location> getResponseType() {
        return Location.class;
    }

    @Override
    public String getPacketType() {
        return "subscribe";
    }

}
