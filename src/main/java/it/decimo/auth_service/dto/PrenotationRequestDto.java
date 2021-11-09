package it.decimo.auth_service.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrenotationRequestDto {
    /**
     * Il locale presso il quale bisogna prenotare
     */
    private int merchantId;

    /**
     * Il numero di posti da allocare
     */
    private int seatsAmount;

    /**
     * La data di prenotazione
     */
    private Date date;

    /**
     * L'id dell'utente che ha mandato la richiesta di prenotazione
     */
    private int requesterId;

    @Override
    public String toString() {
        return String.format("PrenotationRequest(sender: %d, merchantId: %d, seatsAmount: %d, date: %s)", requesterId,
                merchantId, seatsAmount, date);
    }
}
