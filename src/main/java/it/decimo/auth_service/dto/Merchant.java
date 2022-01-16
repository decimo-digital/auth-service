package it.decimo.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Merchant {

    private Integer id;

    private Point storeLocation;

    private Double distance;

    private String cuisineType;

    private String storeDescription;
    private String storeName;
    private Integer owner;
    private Integer freeSeats;
    private Integer totalSeats;
    private double occupancyRate;

    @JsonAnyGetter
    public Map<String, Double> getStoreLocation() {
        if (storeLocation == null) {
            return new HashMap<>();
        }
        return new HashMap<String, Double>() {
            {
                put("lat", storeLocation.getX());
                put("lng", storeLocation.getY());
            }
        };
    }

    public void setStoreLocation(Location location) {
        this.storeLocation = new Point(location.getX(), location.getY());
    }

    @JsonIgnore
    public Point getPoint() {
        return storeLocation;
    }
}
