package it.decimo.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

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
     * <p>
     * Utilizzato solo per scopi di query
     */
    private Date date;

    private int amount;

    private boolean enabled;

    @JsonAlias("type")
    private boolean isValid;

    public java.util.Date getDateOfPrenotation() {
        return new java.util.Date(dateOfPrenotation);
    }
}
