package it.decimo.auth_service.connector;

import it.decimo.auth_service.dto.Merchant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MerchantServiceConnector {

    private final String path = "/api/merchant";
    @Value("${app.connectors.merchantServiceBaseUrl}")
    private String baseUrl;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Recupera la lista di esercenti disponibile
     */
    public List<Merchant> getMerchants() {
        final var builder = new StringBuilder(baseUrl + path);
       
        log.info("Sending request to merchant_service");
        final var response = restTemplate.getForEntity(builder.toString(), List.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Got status code {} from merchant_service while retrieving all merchants", response.getStatusCode());
            return new ArrayList<Merchant>();
        }

        final var list = ((List<Merchant>) response.getBody());

        return list;
    }

    /**
     * Salva l'esercente passato come parametro
     *
     * @return L'entità salvata sul db
     */
    public ResponseEntity<Object> saveMerchant(Merchant toSave) {
        try {
            log.info("Saving merchant {} to user {}", toSave.getStoreName(), toSave.getOwner());
            return restTemplate.postForEntity(baseUrl + path, toSave, Object.class);
        } catch (HttpClientErrorException e) {
            log.warn("Failed to save merchant", e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * Aggiorna i dati relativi al {@link Merchant} il cui id è passato come
     * parametro
     */
    public ResponseEntity<Object> updateMerchantData(int merchantId, Merchant data) {
        try {
            final var response = restTemplate.postForEntity(baseUrl + path + "/{id}/update", data, Object.class, merchantId);
            log.info("Successfully patched merchant {}", merchantId);
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Failed to update merchant {}", merchantId, e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * Ritorna i dati del merchant richiesto
     */
    public Merchant getMerchant(int id) {
        final var url = baseUrl + path + "/{id}/data";

        final var response = restTemplate.getForEntity(url, Merchant.class, id);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Got status code {} while retrieving merchant data", response.getStatusCode());
            return null;
        }

        log.info("Retrieved merchant data");

        return response.getBody();
    }

    /**
     * Elimina il merchant richiesto
     */
    public boolean deleteMerchant(int merchantId, int requesterId) {
        try {
            final var url = baseUrl + path + "/merchantId?requester=" + requesterId;
            restTemplate.delete(url, merchantId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete merchant", e);
            return false;
        }
    }

    /**
     * Recupera i merchant che sono collegati all'utenza richiesta
     *
     * @param userId L'id dell'utenza
     * @return La lista dei merchant collegati all'utenza
     */
    public List<Object> getMerchantsOfUser(int userId) {
        final var url = baseUrl + path + "/user/" + userId;
        final var response = restTemplate.getForEntity(url, List.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Got status code {} while retrieving merchant data", response.getStatusCode());
            return new ArrayList<>();
        }
        return response.getBody();
    }
}
