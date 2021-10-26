package it.decimo.auth_service.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.sql.Statement;

@Service
public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Valida l'accesso dell'utente per effettuare il login
     *
     * @param email    l'email che sta effettuando il login
     * @param password la password (già criptata) da matchare
     * @return Il secret se il login è andato a buon fine, `null` altrimenti
     */
    public boolean hasValidCredentials(String email, String password) {
        String query = "SELECT email FROM auth_users WHERE email = ? AND password = ?";

        try {
            final var found = jdbcTemplate.queryForObject(query, String.class, email, password);
            return found != null;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Registra una nuova utenza nel db
     *
     * @param email    L'email da registrare
     * @param password la password per effettuare la registrazione
     * @return ritorna l'id dell'utente inserito se la registrazione va a buon fine, null altrimenti
     */
    public Integer register(String email, String password) {
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

            String idQuery = "SELECT id FROM auth_users where email = ?";
            return jdbcTemplate.queryForObject(idQuery, Integer.class, email);
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * In caso la {@link #register} non funzioni, questa chiamata esegue una sorta di
     * rollback
     *
     * @param email l'email dell'utenza da eliminare
     */
    public void deleteRegistration(String email) {
        String deleteQuery = "DELETE FROM auth_users WHERE email = ?";
        jdbcTemplate.update(con -> {
            final var statement = con.prepareStatement(deleteQuery);
            statement.setString(1, email);
            return statement;
        });
    }

    /**
     * Controlla se l'utente esiste oppure no
     *
     * @param email l'utente che sta facendo l'accesso
     */
    public boolean userExists(String email) {
        String query = "SELECT email FROM auth_users WHERE email = ?";

        try {
            final var found = jdbcTemplate.queryForObject(query, String.class, email);
            return found != null;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Registra sul db la nuova autenticazione dell'utente
     *
     * @param email L'utente che ha fatto la login
     * @param ip    L'ip recuperato dalla request
     */
    public void logAuthentication(String email, String ip) {
        String idQuery = "SELECT id FROM auth_users WHERE email = ?";

        final var id = jdbcTemplate.queryForObject(idQuery, Integer.class, email);

        if (id == null) {
            logger.error("User {} not found", email);
            throw new NotFoundException("User with email " + email + " doesn't exists");
        }

        String query = "INSERT INTO login_data (user_id, ip) VALUES (?, ?)";

        jdbcTemplate.update(conn -> {
            final var statement = conn.prepareStatement(query);
            statement.setInt(1, id);
            statement.setString(2, ip);
            return statement;
        });
    }
}
