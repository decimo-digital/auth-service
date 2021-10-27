package it.decimo.auth_service.connector.userService;

import it.decimo.auth_service.dto.UserInfoDto;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Setter
public class UserInformationConnector {
    private static final Logger logger = LoggerFactory.getLogger(UserInformationConnector.class);
    private final RestTemplate restTemplate;
    @Value("${app.connectors.userInformationConnector}")
    private String url;

    public UserInformationConnector() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Ritorna le informazioni dell'utente il cui id è passato come parametro
     *
     * @param id L'id dell'utente di cui vogliamo le informazioni
     * @return L'oggetto contenente tutte le informazioni dell'utente
     */
    public UserInfoDto getUserInfo(int id) {
        final var response = restTemplate.getForEntity(url, UserInfoDto.class, id);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            logger.error("Got a {} response from user_service", response.getStatusCode());
            return null;
        }
    }
}
