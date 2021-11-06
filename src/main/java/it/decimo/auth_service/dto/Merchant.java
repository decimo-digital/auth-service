package it.decimo.auth_service.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import org.springframework.data.geo.Point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Merchant {

    private Integer id;

    private Point storeLocation;

    private Double distance;

    @JsonAnyGetter
    public Map<String, Double> getStoreLocation() {
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

    private String storeName;

    private Integer userOwner;

}