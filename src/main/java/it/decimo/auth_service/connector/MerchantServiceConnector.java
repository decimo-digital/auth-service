package it.decimo.auth_service.connector;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.decimo.auth_service.dto.Merchant;
import it.decimo.auth_service.dto.MerchantData;
import it.decimo.auth_service.dto.MerchantStatusDto;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MerchantServiceConnector {

    @Value("${app.connectors.merchantServiceBaseUrl}")
    private String baseUrl;

    private String path = "/api/merchant";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Recupera la lista di esercenti disponibile
     */
    public List<Merchant> getMerchants(Double lat, Double lng) {

        final var builder = new StringBuilder(baseUrl + path);
        if (lat != null && lng != null) {
            builder.append("?lat=" + lat + "&lng=" + lng);
        }

        final var response = restTemplate.getForEntity(builder.toString(), List.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Got status code {} from merchant_service while retrieving all merchants",
                    response.getStatusCode());
            return new ArrayList<Merchant>();
        }

        return ((List<Merchant>) response.getBody());
    }

    /**
     * Salva l'esercente passato come parametro
     * 
     * @return L'entità salvata sul db
     */
    public Merchant saveMerchant(Merchant toSave) {

        final var response = restTemplate.postForEntity(baseUrl + path, toSave, Merchant.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Got status code {} from merchant_service on save", response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * Aggiorna i dati relativi al {@link Merchant} il cui id è passato come
     * parametro
     */
    public boolean updateMerchant(int merchantId, MerchantStatusDto data) {
        try {
            restTemplate.patchForObject(baseUrl + path + "/{id}", data, MerchantData.class, merchantId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ritorna i dati del merchant richiesto
     */
    public MerchantData getMerchantData(int id) {
        final var url = baseUrl + path + "/{id}/data";

        final var response = restTemplate.getForEntity(url, MerchantData.class, id);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Got status code {} while retrieving merchant data", response.getStatusCode());
            return null;
        }

        return response.getBody();
    }
}
