package it.decimo.auth_service.services;

import it.decimo.auth_service.connector.MerchantServiceConnector;
import it.decimo.auth_service.model.AuthUser;
import it.decimo.auth_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MerchantServiceConnector merchantServiceConnector;

    /**
     * Ritorna l'utente richiesto
     */
    public AuthUser getUserInfo(int id) {
        final var user = userRepository.findById(id);
        if (!user.isPresent()) {
            log.info("User of id {} doesn't exists", id);
        }
        return user.orElse(null);
    }

    /**
     * Ritorna tuttii merchant collegati all'utente
     *
     * @param userId id dell'utente
     * @return lista di merchant collegati all'utente
     */
    public List<Object> getConnectedMerchants(Integer userId) {
        log.info("Getting merchants of user {}", userId);
        return merchantServiceConnector.getMerchantsOfUser(userId);
    }
}
