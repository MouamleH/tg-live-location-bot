package team.swiper.location_bot.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationStartEvent implements Serializable {

    private long id;
    private String uuid;

    private long duration;
    private long timestamp;

    private double longitude;
    private double latitude;
    private Double accuracy;

    private int livePeriod;
    private Integer rotation;

    private String username;
    private String userImage;

}
