package it.decimo.auth_service.services;

import it.decimo.auth_service.dto.LoginBody;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.dto.response.LoginResponse;
import it.decimo.auth_service.repository.UserRepository;
import it.decimo.auth_service.utils.exception.ExpiredJWTException;
import it.decimo.auth_service.utils.exception.InvalidJWTBody;
import it.decimo.auth_service.utils.exception.JWTUsernameNotExistingException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Wrapper per i metodi di autologin e di login con credenziali
     *
     * @param jwt  Il jwt da utilizzare per il login automatico
     * @param body Il body da utilizzare per la login con credenziali
     * @return La ResponseEntity pronta da ritornare
     */
    public ResponseEntity<Object> login(String jwt, LoginBody body) {
        if (jwt == null && body == null) {
            return ResponseEntity.badRequest().build();
        }
        if (jwt != null) {
            return autologin(jwt);
        } else {
            final var response = login(body);
            if (response == null) {
                return ResponseEntity.status(401).body(null);
            } else {
                return ResponseEntity.ok(response);
            }
        }
    }

    /**
     * Effettua il login con i dati passati come parametro
     *
     * @param body I dati da utilizzare per effettuare il login
     * @return La coppia di token se la login è andata bene, null altrimenti
     */
    private LoginResponse login(LoginBody body) {
        logger.info("Received login request for user {}", body.getUsername());
        final var canLogin = userRepository.hasValidCredentials(body.getUsername(), body.getPassword());
        if (!canLogin) {
            logger.warn("User {} sent invalid credentials", body.getUsername());
            return null;
        }
        logger.info("User {} has logged in", body.getUsername());

        final var jwt = jwtUtils.generateJwt(body);
        return LoginResponse.builder().accessToken(jwt).build();
    }

    /**
     * Effettua una registrazione utilizzando i dati passati come parametro
     *
     * @param body I dati per la registrazione
     * @return La coppia di token se la registrazione è andata bene, null altrimenti
     */
    public ResponseEntity<Object> register(LoginBody body) {
        logger.info("Registering {}", body.getUsername());
        final var registered = userRepository.register(body.getUsername(), body.getPassword());
        if (!registered) {
            logger.warn("User has sent credentials already in use {}", body.getUsername());
            return ResponseEntity.status(401).body(new BasicResponse("Credentials already in use", "CREDS_ALREAY_USED"));
        }
        final var jwt = jwtUtils.generateJwt(body);

        return ResponseEntity.ok(LoginResponse.builder().accessToken(jwt).build());
    }

    /**
     * Effettua il login automatico tramite l'utilizzo del jwt
     *
     * @param jwt Il jwt da utilizzare
     * @return La coppia di token se la registrazione è andata bene, null altrimenti
     */
    private ResponseEntity<Object> autologin(String jwt) {
        logger.info("Received autologin request");
        if (jwt == null) {
            return ResponseEntity.status(400).body(new BasicResponse("Missing access-token", "NO_ACCESS_TOKEN"));
        }
        try {
            final var isValid = jwtUtils.isJwtValid(jwt);
            if (isValid) {
                return ResponseEntity.ok(LoginResponse.builder().accessToken(jwt).build());
            } else {
                return ResponseEntity.status(401).body(new BasicResponse("You have to re-login", "TOKEN_NOT_VALID"));
            }
        } catch (InvalidJWTBody e) {
            return ResponseEntity.status(422).body(new BasicResponse("Jwt doesn't contain all the supposed fields", "INVALID_TOKEN"));
        } catch (ExpiredJWTException e) {
            return ResponseEntity.status(422).body(new BasicResponse("Access token was expired", "EXPIRED_TOKEN"));
        } catch (JWTUsernameNotExistingException e) {
            return ResponseEntity.status(404).body(new BasicResponse("Username contained in the JWT doesn't exists", "INVALID_TOKEN"));
        }
    }

    /**
     * Wrapper della {@link #logNewLogin} ma che accetta il JWT
     *
     * @param jwt Il jwt dal quale recuperare l'username
     * @param ip  L'indirizzo recuperato dalla request
     */
    @SneakyThrows
    public void logNewLoginFromJwt(String jwt, String ip) {
        final var username = ((String) jwtUtils.extractField(jwt, "username"));
        logNewLogin(username, ip);
    }

    /**
     * Registra la nuova login sul db per l'utente
     *
     * @param username L'utente per cui registrare l'accesso
     * @param ip       l'indirizzo recuperato dalla request
     */
    public void logNewLogin(String username, String ip) {
        userRepository.logAuthentication(username, ip);
    }
}
