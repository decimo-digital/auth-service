package it.decimo.auth_service.connector.userService;

import it.decimo.auth_service.dto.RegistrationDto;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Setter
public class UserRegistrationConnector {
    @Value("${app.connectors.userRegistrationConnector}")
    private String url;
    private RestTemplate restTemplate;

    public UserRegistrationConnector() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Invia la chiamata allo User_Service per registrare l'utenza
     *
     * @param body i dati per la registrazione
     * @return true se la registrazione è andata a buon fine, false altrimenti
     */
    public boolean register(RegistrationDto body) {
        final var response = restTemplate.postForEntity(url, body, Map.class);
        return response.getStatusCode() == HttpStatus.OK;
    }
}
