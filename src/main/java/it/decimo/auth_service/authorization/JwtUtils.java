package it.decimo.auth_service.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.decimo.auth_service.configs.AppConfig;
import it.decimo.auth_service.dto.LoginBody;
import it.decimo.auth_service.repository.UserRepository;
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
     * Formatta una stringa in bytes {@param bytes} in una stringa "0x****"
     *
     * @return la stringa formattata
     */
    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    /**
     * Calcola l'HMAC sui dati {@param data} utilizzando il secret {@param key }
     */
    public static String calculateHMAC(String data, String key)
            throws
            NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA512);
        Mac mac = Mac.getInstance(HMAC_SHA512);
        mac.init(secretKeySpec);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    /**
     * Genera un JWT a partire da un {@param secret} e dei dati di login {@param loginData}
     *
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
     * Controlla se il {@param jwt} è ancora in corso di validità
     */
    @SneakyThrows
    public boolean isJwtValid(String jwt) {
        final var payload = jwt.substring(0, jwt.lastIndexOf("."));
        final var newSignature = calculateHMAC(payload, appConfig.getJwtSecret());

        final var oldSignature = jwt.substring(jwt.lastIndexOf(".") + 1);


        if (!newSignature.equals(oldSignature)) {
            logger.error("Sent a JWT with an invalid signature");
            return false;
        }

        final var base64 = new Base64();
        final var body = mapper.readValue(base64.decode(payload.substring(payload.indexOf("."))), Map.class);
        final var props = new ArrayList<String>() {{
            add("username");
            add("iat");
            add("exp");
        }};

        if (props.stream().anyMatch(prop -> !body.containsKey(prop))) {
            logger.error("Sent jwt didn't have all the required props");
            return false;
        }

        final var exp = Date.from(Instant.ofEpochMilli(Long.parseLong(((String) body.get("exp")))));
        if (new Date().after(exp)) {
            logger.error("Sent jwt was expired");
            return false;
        }

        final var username = ((String) body.get("username"));
        if (!userRepository.userExists(username)) {
            logger.error("Username {} doesn't exists", username);
            return false;
        }

        return true;
    }
}
