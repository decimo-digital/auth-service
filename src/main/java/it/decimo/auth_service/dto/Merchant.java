package it.decimo.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Merchant {
    private Integer id;
    @JsonAlias(value = "location")
    private Map<String, Double> storeLocation;
    private boolean isEnabled;
    @JsonAlias(value = "store_name")
    private String storeName;
    @JsonAlias(value = "owner")
    private Integer owner;
    @JsonAlias(value = "free_seats")
    private Integer freeSeats;
    private String description;
    @JsonAlias(value = "occupancy_rate")
    private float occupancyRate;
    @JsonAlias(value = "total_seats")
    private Integer totalSeats;
    private String cuisineType;
    private String image;
}
