package it.decimo.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantStatusDto {
    private int id;
    private Integer totalSeats;
    private Integer freeSeats;
}
