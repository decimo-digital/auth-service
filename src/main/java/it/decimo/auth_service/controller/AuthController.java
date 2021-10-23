package it.decimo.auth_service.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.decimo.auth_service.dto.LoginBody;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.dto.response.LoginResponse;
import it.decimo.auth_service.services.AuthService;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/api/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/login", produces = {"application/json"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LoginResponse.class)), description = "Il login è andato bene"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "Non è stato possibile effettuare il login"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "L'username contenuto nel JWT non esiste nel db"),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "JWT scaduto o formattatno male"),
    })
    public ResponseEntity<Object> login(@RequestHeader(value = "access-token", required = false) String jwt, @RequestBody(required = false) LoginBody body) {
        return authService.login(jwt, body);
    }

    @PostMapping(value = "/register", produces = {"application/json"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LoginResponse.class)), description = "La registrazione è andata bene"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "Non è stato possibile effettuare la registrazione"),
    })
    @SneakyThrows
    public ResponseEntity<Object> register(@RequestBody LoginBody body) {
        return authService.register(body);
    }

    @GetMapping("/test")
    @NeedLogin
    public ResponseEntity<Object> test(@RequestHeader(value = "access-token", required = false) String accessToken) {
        return ResponseEntity.ok(new BasicResponse("Yupw", "OK"));
    }
}
