package it.decimo.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantData {

    @JsonAlias("merchant_id")
    private int merchantId;

    @JsonAlias("total_seats")
    private int totalSeats;

    private String description;
}
