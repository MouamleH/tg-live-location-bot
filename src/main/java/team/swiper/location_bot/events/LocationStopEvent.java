package team.swiper.location_bot.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationStopEvent implements Serializable {

    private long id;
    private String uuid;

    private double longitude;
    private double latitude;

}
