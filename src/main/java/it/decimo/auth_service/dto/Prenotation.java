package it.decimo.auth_service.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Prenotation {

    private int id;

    private int owner;

    private int merchantId;

    private Date dateOfPrenotation;

    private int amount;

    private boolean isValid;
}
