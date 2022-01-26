package it.decimo.auth_service.connector;

import it.decimo.auth_service.dto.MenuCategory;
import it.decimo.auth_service.dto.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
@Slf4j
public class MenuConnector {

    private final String path = "/api/merchant/{id}/menu";
    @Value("${app.connectors.merchantServiceBaseUrl}")
    private String baseUrl;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Aggiorna un elemento del menu di un merchant
     */
    public ResponseEntity<Object> updateMenuItem(int merchantId, MenuItem item, int requesterId) {
        try {
            log.info("Updating menu item in {}", merchantId);
            final var url = (baseUrl + path + "?requester=" + requesterId).replace("{id}", Integer.toString(merchantId));
            restTemplate.patchForObject(url, item, Object.class);
            return ResponseEntity.ok().build();
        } catch (HttpClientErrorException e) {
            log.warn("Failed to update menu item: {} of merchant {}", item.getMenuItemId(), merchantId, e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    /**
     * Recupera il menu del merchant
     *
     * @param merchantId Il merchant di cui ci interessa il menu
     */
    public List<MenuItem> getMenu(int merchantId) {
        log.info("Getting menu for merchant {}", merchantId);
        return restTemplate.getForObject((baseUrl + path).replace("{id}", Integer.toString(merchantId)), List.class);
    }

    /**
     * Aggiunge un item al menu del merchant
     *
     * @param merchantId Il merchant di cui aggiungere l'item
     * @param item       L'item da aggiungere
     */
    public ResponseEntity<Object> insertItem(int merchantId, MenuItem item, int requesterId) {
        log.info("Adding menu item to {}", merchantId);
        final var entity = restTemplate.postForEntity((baseUrl + path + "?requester=" + requesterId).replace("{id}", Integer.toString(merchantId)), item, Object.class);
        if (entity.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to insert menu item: {}", entity.getBody());
        }
        return entity;
    }

    /**
     * Elimina un item dal menu del merchant
     *
     * @param merchantId Il merchant di cui eliminare l'item
     * @param itemId     L'id dell'item da eliminare
     */
    public void deleteMenuItem(int merchantId, int itemId, int requesterId) {
        log.info("Deleting menu item from {}", merchantId);
        try {
            restTemplate.delete((baseUrl + path + "/" + itemId + "?requester=" + requesterId).replace("{id}", Integer.toString(merchantId)));
        } catch (Exception e) {
            log.error("Failed to delete menu item: {}", e.getMessage());
        }
    }

    /**
     * Recupera tutte le categorie di piatti possibili
     */
    public List<MenuCategory> getCategories() {
        return restTemplate.getForObject((baseUrl + path + "/categories").replace("{id}", "0"), List.class);
    }
}
