package it.decimo.auth_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.decimo.auth_service.configs.AppConfig;
import it.decimo.auth_service.dto.LoginBody;
import it.decimo.auth_service.repository.UserRepository;
import it.decimo.auth_service.utils.exception.*;
import lombok.SneakyThrows;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

/**
 * Classe che contiene tutte le utility per la generazione e la validazione
 * dell'header JWT per l'autenticazione
 */
@Service
public class JwtUtils {
    private static final String HMAC_SHA512 = "HmacSHA512";
    /**
     * Il time to live del JWT di accesso
     */
    final long jwtTtl = 7200000;
    final String alg = "HS512";
    final String type = "JWT";
    private final AppConfig appConfig;
    private final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private final ObjectMapper mapper;
    private final UserRepository userRepository;

    public JwtUtils(ObjectMapper mapper, AppConfig appConfig, UserRepository userRepository) {
        this.mapper = mapper;
        this.appConfig = appConfig;
        this.userRepository = userRepository;
    }

    /**
     * Calcola l'HMAC sui dati utilizzando il secret
     *
     * @param data I dati che comprendono HEADER + BODY
     * @param key  Il secret da utilizzare per il caclolo dell'HMAC
     */
    private String calculateHMAC(String data, String key)
            throws
            NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA512);
        Mac mac = Mac.getInstance(HMAC_SHA512);
        mac.init(secretKeySpec);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    /**
     * Estrae il campo fieldName dal jwt, se questo esiste
     *
     * @param fieldName Il nome del campo da estrarre
     * @param jwt       Il JWT completo di partenza
     * @throws MissingKeyException           se la chiave {@param fieldName} non esiste all'interno del {@param jwt}
     * @throws InvalidPayloadFormatException se il {@param jwt} contiene un payload formattato male
     */
    private Object extractField(String jwt, String fieldName) throws MissingKeyException, InvalidPayloadFormatException {
        final var payload = jwt.substring(jwt.indexOf(".") + 1, jwt.lastIndexOf("."));
        final var payloadAsMap = payloadToMap(payload);
        if (!payloadAsMap.containsKey(fieldName)) {
            throw new MissingKeyException("Missing key: " + fieldName);
        }
        return payloadAsMap.get(fieldName);
    }

    /**
     * Dato il payload di un JWT, lo ritorna sotto forma di mappa
     *
     * @param payload La parte relativa al body del jwt
     * @throws InvalidPayloadFormatException Se il {@param payload} non è formattato correttamente
     */
    private Map payloadToMap(String payload) throws InvalidPayloadFormatException {
        try {
            final var base64 = new Base64();
            final var bytes = base64.decode(payload.getBytes(StandardCharsets.UTF_8));
            return mapper.readValue(bytes, Map.class);
        } catch (Exception e) {
            throw new InvalidPayloadFormatException(e.getMessage());
        }

    }

    /**
     * Formatta una stringa in bytes in una stringa "0x****"
     *
     * @param bytes I byte da trasformare in HEX
     * @return la stringa formattata
     */
    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    /**
     * Genera un JWT a partire dai dati di login
     *
     * @param loginData I dati di login da mettere nel JWT
     * @return il JWT da inviare per le prossime richieste
     */
    @SneakyThrows
    public String generateJwt(LoginBody loginData) {
        final var iat = new Date().toInstant().toEpochMilli();

        Map<String, String> header = new HashMap<>() {{
            put("alg", alg);
            put("typ", type);
        }};

        Map<String, String> payload = new HashMap<>() {{
            put("username", loginData.getUsername());
            put("iat", Long.toString(iat));
            put("exp", Long.toString(iat + jwtTtl));
        }};

        final var headerBytes = mapper.writeValueAsString(header).getBytes(StandardCharsets.UTF_8);
        final var payloadBytes = mapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);

        final var base64 = new Base64();
        final var data = base64.encodeToString(headerBytes) + "." + base64.encodeToString(payloadBytes);
        final var signature = calculateHMAC(data, appConfig.getJwtSecret());

        return data + "." + signature;
    }


    /**
     * Controlla se il jwt è ancora in corso di validità
     *
     * @param jwt Il JWT da controllare
     * @throws ExpiredJWTException             Se il JWT è scaduto
     * @throws JWTUsernameNotExistingException Se l'email contenuta nel JWT non fa riferimento a nessun utente nel DB
     * @throws InvalidJWTBody                  Se manca un qualche campo atteso all'interno del body del JWT
     */
    public boolean isJwtValid(String jwt) throws ExpiredJWTException, JWTUsernameNotExistingException, InvalidJWTBody {
        try {
            final var payload = jwt.substring(0, jwt.lastIndexOf("."));
            final var newSignature = calculateHMAC(payload, appConfig.getJwtSecret());

            final var oldSignature = jwt.substring(jwt.lastIndexOf(".") + 1);


            if (!newSignature.equals(oldSignature)) {
                logger.error("Sent a JWT with an invalid signature");
                return false;
            }

            final var body = payloadToMap(payload.substring(payload.indexOf(".")));

            final var props = new ArrayList<String>() {{
                add("username");
                add("iat");
                add("exp");
            }};

            if (props.stream().anyMatch(prop -> !body.containsKey(prop))) {
                logger.error("Sent jwt didn't have all the required props");
                throw new InvalidJWTBody();
            }

            final var exp = Date.from(Instant.ofEpochMilli(Long.parseLong(((String) body.get("exp")))));
            if (new Date().after(exp)) {
                logger.error("Sent jwt was expired");
                throw new ExpiredJWTException();
            }

            final var username = ((String) body.get("username"));
            if (!userRepository.userExists(username)) {
                logger.error("Username {} doesn't exists", username);
                throw new JWTUsernameNotExistingException();
            }

            return true;
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidPayloadFormatException e) {
            logger.error("Caught exception", e);
            return false;
        }
    }
}
