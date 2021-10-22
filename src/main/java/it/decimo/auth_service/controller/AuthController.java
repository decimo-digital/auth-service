package it.decimo.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.decimo.auth_service.authorization.JwtUtils;
import it.decimo.auth_service.dto.LoginBody;
import it.decimo.auth_service.repository.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping(path = "/api/auth")
@Slf4j
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository userRepository;
    private JwtUtils jwtUtils;

    public AuthController(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    ResponseEntity<String> login(@RequestBody LoginBody body) {
        logger.info("Received login request for user {}", body.getUsername());
        final String secret = userRepository.hasValidCredentials(body.getUsername(), body.getPassword());
        if (secret == null) {
            logger.warn("User {} sent invalid credentials", body.getUsername());
            return ResponseEntity.status(401).body("Unauthorized");
        }
        //TODO generare JWT
        logger.info("User {} has logged in", body.getUsername());
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/register")
    @SneakyThrows
    ResponseEntity<String> register(@RequestBody LoginBody body) {
        logger.info("Registering {}", body.getUsername());
        final var registered = userRepository.register(body.getUsername(), body.getPassword());
        if (!registered) {
            logger.warn("User has sent credentials already in use {}", body.getUsername());
            return ResponseEntity.status(422).body("Credentials already in use");
        }
        final var jwt = jwtUtils.generateJwt(body);
        return ResponseEntity.ok(new ObjectMapper().writeValueAsString(new HashMap<String, String>() {{
            put("access-token", jwt);
        }}));
    }

    @PostMapping("/auto")
    ResponseEntity<String> autoLogin(@RequestHeader(value = "access-token") String jwt) {
        logger.info("Received autologin request");
        if (jwt == null) {
            return ResponseEntity.status(401).body("Missing access-token");
        }
        final var isValid = jwtUtils.isJwtValid(jwt);
        if (isValid) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.status(401).body("You have to re-login");
        }

    }
}
