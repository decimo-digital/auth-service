package it.decimo.auth_service.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Controlla se l'utente richiesto è un merchant oppure no
     *
     * @param userId id dell'utente
     * @return true se l'utente è un merchant, false altrimenti
     */
    public boolean isUserAMerchant(int userId) {
        final var query = "SELECT * FROM merchant WHERE owner = " + userId;
        final var response = jdbcTemplate.queryForList(query);
        return !response.isEmpty();
    }
}
