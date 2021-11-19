package it.decimo.auth_service.connector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.decimo.auth_service.dto.PrenotationRequestDto;

@Component
public class PrenotationServiceConnector {
    @Value("${app.connectors.prenotationServiceBaseUrl}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    private String path = "/api/prenotation";

    /**
     * Invia al PrenotationService la richiesta di prenotazione
     * 
     * @return {@literal true} se la prenotazione è andata bene, {@literal false}
     *         altrimenti
     */
    public ResponseEntity<Object> makePrenotation(PrenotationRequestDto request) {
        return restTemplate.postForEntity(baseUrl + path, request, Object.class);
    }

    /**
     * Ritorna la lista di prenotazioni dell'utente
     */
    public ResponseEntity<Object> getPrenotations(int userId) {
        return restTemplate.getForEntity(baseUrl + path + "/" + userId, Object.class);
    }

    /**
     * Aggiunge un utente ad una prenotazione esistente
     */
    public ResponseEntity<Object> addUserToPrenotation(int requesterId, int prenotationId, int userId) {
        final var url = baseUrl + path + "/" + prenotationId + "?userId=" + userId + "&requesterId=" + requesterId;
        return restTemplate.postForEntity(url, null, Object.class);
    }

}
