package it.decimo.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantData {

    private int merchantId;

    // @Column(name = "openings")
    // private String openings;

    private int freeSeats;

    private int totalSeats;
}
