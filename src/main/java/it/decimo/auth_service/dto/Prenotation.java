package it.decimo.auth_service.dto;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonAlias;

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

    /**
     * Contiene la data di effettuata prenotazione (comprensiva di tempo)
     */
    private long dateOfPrenotation;

    /**
     * Contiene giorno-mese-anno della prenotazione
     * 
     * Utilizzato solo per scopi di query
     */
    private Date date;

    private int amount;

    @JsonAlias("type")
    private boolean isValid;

    public java.util.Date getDateOfPrenotation() {
        return new java.util.Date(dateOfPrenotation);
    }
}
