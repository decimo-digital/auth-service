package it.decimo.auth_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import it.decimo.auth_service.dto.LoginBody;
import it.decimo.auth_service.dto.RegistrationDto;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.dto.response.LoginResponse;
import it.decimo.auth_service.model.AuthUser;
import it.decimo.auth_service.repository.UserRepository;
import it.decimo.auth_service.utils.exception.ExpiredJWTException;
import it.decimo.auth_service.utils.exception.InvalidJWTBody;
import it.decimo.auth_service.utils.exception.JWTUsernameNotExistingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper mapper;

    @Value("${google.client.id}")
    private String googleClientId;

    /**
     * Recupera l'id dell'utente che ha inviato la richiesta
     *
     * @param jwt Il JWT per l'autorizzazione della richiesta
     * @return L'id dell'utente corrispondente
     */
    @SneakyThrows
    public int getIdFromJwt(String jwt) {
        final var email = ((String) jwtUtils.extractField(jwt, "username"));
        return getIdFromEmail(email);
    }

    /**
     * Ritorna l'id collegato all'email
     */
    public int getIdFromEmail(String email) {
        return userRepository.findByEmail(email).get().getId();
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
            log.error("Missing both jwt and credentials");
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
        log.info("Received login request for user {}", body.getUsername());
        final var canLogin = userRepository.findByEmailAndPassword(body.getUsername(), body.getPassword()).isPresent();
        if (!canLogin) {
            log.warn("User {} sent invalid credentials", body.getUsername());
            return null;
        }
        log.info("User {} has logged in", body.getUsername());

        final var jwt = jwtUtils.generateJwt(body);
        return LoginResponse.builder().accessToken(jwt).build();
    }

    /**
     * Effettua una registrazione utilizzando i dati passati come parametro
     *
     * @param body I dati per la registrazione
     * @return La coppia di token se la registrazione è andata bene, null altrimenti
     */
    public ResponseEntity<Object> register(RegistrationDto body) {
        log.info("Registering {}", body.getEmail());
        AuthUser user = AuthUser.builder().email(body.getEmail()).password(body.getPassword()).firstName(body.getFirstName()).lastName(body.getLastName()).build();

        if (!userRepository.findByEmail(user.getEmail()).isEmpty()) {
            log.warn("User has sent credentials already in use {}", body.getEmail());
            return ResponseEntity.status(401).body(new BasicResponse("Credentials already in use", "CREDS_ALREAY_USED"));
        }
        try {
            user = userRepository.save(user);
        } catch (Exception e) {
            log.error("Error while saving user {}: {}", body.getEmail(), e.getMessage());
            return ResponseEntity.status(401).body(new BasicResponse("Error while saving user", "ERROR_SAVING_USER"));
        }

        final String jwt = jwtUtils.generateJwt(LoginBody.builder().username(user.getEmail()).build());

        return ResponseEntity.ok(LoginResponse.builder().accessToken(jwt).build());
    }

    /**
     * Effettua il login automatico tramite l'utilizzo del jwt
     *
     * @param jwt Il jwt da utilizzare
     * @return La coppia di token se la registrazione è andata bene, null altrimenti
     */
    private ResponseEntity<Object> autologin(String jwt) {
        log.info("Received autologin request");
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
     * @param tokenId Il token da utilizzare per l'accesso
     * @return L'access-token per autenticare le chiamate successive
     * @throws JsonProcessingException se fallisce il recupero dei certificati di google per garantire l'integrità del tokenId
     */
    public LoginResponse googleSignIn(String tokenId) throws IOException, GeneralSecurityException {
        try {
            final var transport = new NetHttpTransport();
            final var factory = new GsonFactory();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, factory).setAudience(Collections.singletonList(googleClientId)).build();
            log.info("Verifying google token {}", tokenId);
            GoogleIdToken token = verifier.verify(tokenId);
            AuthUser registeredUser;
            if (token != null) {
                log.info("Received valid token");
                GoogleIdToken.Payload payload = token.getPayload();
                String userId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String familyName = (String) payload.get("family_name");

                final var user = AuthUser.builder().email(email).firstName(name).lastName(familyName).googleId(userId).build();
                log.info("Created user from token: {}", email);
                final var savedUser = userRepository.findByEmail(user.getEmail());

                if (savedUser.isEmpty()) {
                    log.info("Registering {}", user.getEmail());
                    registeredUser = userRepository.save(user);
                } else {
                    log.info("User {} already registered", user.getEmail());
                    registeredUser = savedUser.get();
                }
            } else {
                log.error("Received id token is not valid");
                return null;
            }

            final var accessToken = jwtUtils.generateJwt(LoginBody.builder().username(registeredUser.getEmail()).build());
            log.info("Generated jwt for user {}", registeredUser.getEmail());
            return LoginResponse.builder().accessToken(accessToken).build();
        } catch (Exception e) {
            log.error("Error while logging in with google: {}", e.getMessage());
            return null;
        }
    }

}
