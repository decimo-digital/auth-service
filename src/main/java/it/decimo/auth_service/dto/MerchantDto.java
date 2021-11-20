package it.decimo.auth_service.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.geo.Point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDto {

    private Integer id;

    private Point storeLocation;

    /**
     * Contiene la distanza che viene calcolata al momento della query per ciascun
     * client
     */
    private Double distance;

    private int freeSeats;
    private int totalSeats;

    public Point getPoint() {
        return storeLocation;
    }

    @JsonIgnore
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

    private Integer owner;
}
