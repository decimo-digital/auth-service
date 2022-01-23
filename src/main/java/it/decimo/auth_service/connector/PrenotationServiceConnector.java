package it.decimo.auth_service.connector;

import it.decimo.auth_service.dto.Prenotation;
import it.decimo.auth_service.dto.PrenotationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class PrenotationServiceConnector {
    private final String path = "/api/prenotation";
    @Value("${app.connectors.prenotationServiceBaseUrl}")
    private String baseUrl;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Invia al PrenotationService la richiesta di prenotazione
     *
     * @return {@literal true} se la prenotazione è andata bene, {@literal false}
     * altrimenti
     */
    public ResponseEntity<Object> makePrenotation(PrenotationRequestDto request) {
        try {
            log.info("User {} is prenotating on merchant {}", request.getRequesterId(), request.getMerchantId());
            return restTemplate.postForEntity(baseUrl + path, request, Object.class);
        } catch (HttpClientErrorException e) {
            log.warn("Failed to make prenotation {}", request, e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * Ritorna la lista di prenotazioni dell'utente
     */
    public ResponseEntity<List> getPrenotations(int userId) {
        final var response = restTemplate.getForEntity(baseUrl + path + "/" + userId, List.class);

        final var list = response.getBody();

        log.info("Received {} prenotations", list.size());
        return response;
    }

    public ResponseEntity<List> getPrenotationsForMerchant(int merchantId, int requesterId) {
        final var response = restTemplate
                .getForEntity(baseUrl + path + "/" + merchantId + "/prenotations?userId=" + requesterId, List.class);
        log.info("Received {} prenotations for merchant {}", response.getBody().size(), merchantId);
        return response;
    }

    public ResponseEntity<Object> deletePrenotation(int prenotationId, int requesterId) {
        try {
            final var url = baseUrl + path + "/" + prenotationId + "?userId=" + requesterId;
            restTemplate.delete(url, null, Object.class);
            log.info("Deleted prenotation {}", prenotationId);
            return ResponseEntity.ok().build();
        } catch (HttpClientErrorException e) {
            log.warn("Got http error: {}", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.info("Error deleting prenotation {}", prenotationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Aggiunge un utente ad una prenotazione esistente
     */
    public ResponseEntity<Object> addUserToPrenotation(int requesterId, int prenotationId, int userId) {
        final var url = baseUrl + path + "/" + prenotationId + "?userId=" + userId + "&requesterId=" + requesterId;
        return restTemplate.postForEntity(url, null, Object.class);
    }

    /**
     * Aggiorna una prenotazione
     */
    public ResponseEntity<Object> updatePrenotation(int requesterId, Prenotation prenotation) {
        try {
            final var url = baseUrl + path + "/update?userId=" + requesterId;
            final var result = restTemplate.postForEntity(url, prenotation, Object.class);
            log.info("Updated prenotation {}", prenotation.getId());
            return result;
        } catch (HttpClientErrorException e) {
            log.warn("Failed to update prenotation {}", prenotation, e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}
