package team.swiper.location_bot.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateEvent implements Serializable {

    private long id;
    private String uuid;

    private long duration;

    private double longitude;
    private double latitude;
    private Double accuracy;

    private Integer rotation;

}
