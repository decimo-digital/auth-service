package it.decimo.auth_service.connector;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.decimo.auth_service.dto.RegistrationDto;
import it.decimo.auth_service.dto.UserInfoDto;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Setter
@Slf4j
public class UserServiceConnector {
    @Value("${app.connectors.userServiceBaseUrl}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Invia la chiamata allo User_Service per registrare l'utenza
     *
     * @param body i dati per la registrazione
     * @return true se la registrazione è andata a buon fine, false altrimenti
     */
    public boolean register(RegistrationDto body) {
        try {

            final var response = restTemplate.postForEntity(baseUrl + "/api/user/register", body, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Something went wrong with the connection to user_service: {}", e);
            return false;
        }
    }

    /**
     * Ritorna le informazioni dell'utente il cui id è passato come parametro
     *
     * @param id L'id dell'utente di cui vogliamo le informazioni
     * @return L'oggetto contenente tutte le informazioni dell'utente
     */
    public UserInfoDto getUserInfo(int id) {
        final var response = restTemplate.getForEntity(baseUrl + "/api/user/{id}/info", UserInfoDto.class, id);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            log.error("Got a {} response from user_service", response.getStatusCode());
            return null;
        }
    }
}
