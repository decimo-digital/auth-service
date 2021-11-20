package it.decimo.auth_service.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.geo.Point;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDto {

    public MerchantDto(Merchant merchant, MerchantData data) {
        this.id = merchant.getId();
        this.storeLocation = merchant.getPoint();
        this.distance = merchant.getDistance();
        this.storeName = merchant.getStoreName();
        this.owner = merchant.getUserOwner();
        this.data = data;
    }

    private Integer id;

    private Point storeLocation;

    /**
     * Contiene la distanza che viene calcolata al momento della query per ciascun
     * client
     */

    private Double distance;

    private MerchantData data;

    public Point getPoint() {
        return storeLocation;
    }

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
