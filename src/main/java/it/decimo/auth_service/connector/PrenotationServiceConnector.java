package it.decimo.auth_service.connector;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.decimo.auth_service.dto.Prenotation;
import it.decimo.auth_service.dto.PrenotationRequestDto;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
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
    public List<Prenotation> getPrenotations(int userId) {
        final var response = restTemplate.getForEntity(baseUrl + path + "/" + userId, List.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            return new ArrayList<Prenotation>();
        }
        final var list = ((List<Prenotation>) response.getBody());

        log.info("Received {} prenotations", list.size());
        return list;
    }

    /**
     * Aggiunge un utente ad una prenotazione esistente
     */
    public ResponseEntity<Object> addUserToPrenotation(int requesterId, int prenotationId, int userId) {
        final var url = baseUrl + path + "/" + prenotationId + "?userId=" + userId + "&requesterId=" + requesterId;
        return restTemplate.postForEntity(url, null, Object.class);
    }

}
