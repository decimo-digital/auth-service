package it.decimo.auth_service.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Statement;

@Service
public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Valida l'accesso dell'utente {@param username} con la {@param password}
     *
     * @return Il secret se il login è andato a buon fine, `null` altrimenti
     */
    public String hasValidCredentials(String email, String password) {
        String query = "SELECT secret FROM auth_users where email = " + email + " AND password = " + password;
        return jdbcTemplate.queryForObject(query, String.class);
    }

    /**
     * Registra una nuova utenza nel db e gli associa un secret
     *
     * @return `true` se l'inserimento è andato a buon fine, `false` altrimenti
     */
    public boolean register(String email, String password) {
        try {
            String insertQuery = "INSERT INTO auth_users (email, password) values (?, ?)";

            KeyHolder holder = new GeneratedKeyHolder();

            logger.info("Inserting new user in auth_users");

            jdbcTemplate.update(connection -> {
                final var ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, email);
                ps.setString(2, password);
                return ps;
            }, holder);

            return true;
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Controlla se l'utente {@param username} esiste oppure no
     */
    public boolean userExists(String username) {
        String query = "SELECT email FROM auth_users WHERE email = ?";

        try {
            final var found = jdbcTemplate.queryForObject(query, String.class, username);
            return found != null;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
