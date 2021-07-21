package team.swiper.location_bot.telegram;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import team.swiper.location_bot.events.LocationStartEvent;
import team.swiper.location_bot.events.LocationStopEvent;
import team.swiper.location_bot.events.LocationUpdateEvent;
import team.swiper.location_bot.util.IDLinker;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
public class LiveLocationBot extends AbilityBot {
    private static final Logger logger = LoggerFactory.getLogger(LiveLocationBot.class);

    private final IDLinker idLinker = IDLinker.getInstance();

    public LiveLocationBot(String token, String username) {
        super(token, username, new DisabledAbilities());
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Starts The Bot")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.send("Send me a live location :)", ctx.chatId()))
                .build();
    }

    public Reply onLocation() {
        return Reply.of((bot, update) -> {
            final Message message = update.getMessage();
            final Location location = message.getLocation();
            final User from = message.getFrom();

            if (location.getLivePeriod() == null) {
                silent.send("Only supports live location!", message.getChatId());
                return;
            }

            final String uuid = idLinker.linkId(message.getMessageId(), location.getLivePeriod() * 1000);
            final String username = String.format("%s %s", from.getFirstName(), from.getLastName() != null ? from.getLastName() : "");

            long endTime = location.getLivePeriod() + message.getDate();
            long endDuration = endTime - (System.currentTimeMillis() / 1000);

            final LocationStartEvent event = new LocationStartEvent();
            event.setId(from.getId());
            event.setUuid(uuid);
            event.setUsername(username.trim());
            event.setLongitude(location.getLongitude());
            event.setLatitude(location.getLatitude());
            event.setRotation(location.getHeading());
            event.setAccuracy(location.getHorizontalAccuracy());
            event.setLivePeriod(location.getLivePeriod());
            event.setTimestamp(message.getDate());
            event.setDuration(endDuration);

            final Optional<String> oImage = getUserImageBase64(from.getId());
            oImage.ifPresent(event::setUserImage);

            EventBus.getDefault().post(event);

            final String sendMessage = String.format("Track Location At\n http://location.swiper.team/%s", uuid);
            silent.send(sendMessage, from.getId());
        }, Flag.MESSAGE, Flag.LOCATION);
    }

    public Reply onLocationUpdate() {
        return Reply.of((bot, update) -> {
            final Message message = update.getEditedMessage();
            final Location location = message.getLocation();
            final User from = message.getFrom();

            final Optional<String> uuid = idLinker.getId(message.getMessageId());
            if (!uuid.isPresent()) {
                logger.warn("Could not find linked uuid to message id ({})", message.getMessageId());
                return;
            }

            if (location.getLivePeriod() == null) {
                final LocationStopEvent event = new LocationStopEvent();
                event.setId(from.getId());
                event.setUuid(uuid.get());
                event.setLongitude(location.getLongitude());
                event.setLatitude(location.getLatitude());
                EventBus.getDefault().post(event);
                idLinker.clear(message.getMessageId());
                return;
            }

            long endTime = location.getLivePeriod() + message.getDate();
            long endDuration = endTime - (System.currentTimeMillis() / 1000);

            final LocationUpdateEvent event = new LocationUpdateEvent();
            event.setId(from.getId());
            event.setUuid(uuid.get());
            event.setLongitude(location.getLongitude());
            event.setLatitude(location.getLatitude());
            event.setRotation(location.getHeading());
            event.setAccuracy(location.getHorizontalAccuracy());
            event.setDuration(endDuration);
            EventBus.getDefault().post(event);
        }, Flag.EDITED_MESSAGE, update -> update.getEditedMessage().hasLocation());
    }

    private Optional<String> getUserImageBase64(long userId) {
        final GetUserProfilePhotos get = GetUserProfilePhotos.builder()
                .userId(userId)
                .build();

        final UserProfilePhotos userProfilePhotos;
        try {
            userProfilePhotos = sender.execute(get);
        } catch (TelegramApiException e) {
            logger.warn("could not get user profile photos", e);
            return Optional.empty();
        }

        final List<List<PhotoSize>> photos = userProfilePhotos.getPhotos();
        final List<PhotoSize> photoSizes = photos.get(0);
        final PhotoSize photoSize = photoSizes.get(photoSizes.size() - 1);

        final GetFile getFile = GetFile.builder()
                .fileId(photoSize.getFileId())
                .build();

        final File file;
        try {
            file = sender.execute(getFile);
        } catch (TelegramApiException e) {
            logger.warn("could not get user profile photo file", e);
            return Optional.empty();
        }

        try {
            final InputStream inputStream = downloadFileAsStream(file);
            final byte[] bytes = IOUtils.toByteArray(inputStream);
            final String base64 = Base64.encodeBase64String(bytes);
            return Optional.of(base64);
        } catch (TelegramApiException e) {
            logger.warn("could not download file", e);
        } catch (IOException e) {
            logger.warn("Caught exception when converting to Base64", e);
        }

        return Optional.empty();
    }

    @Override
    public long creatorId() {
        return 121414901;
    }

}
