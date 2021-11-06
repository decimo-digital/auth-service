package it.decimo.auth_service.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.decimo.auth_service.dto.LoginBody;
import it.decimo.auth_service.dto.RegistrationDto;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.dto.response.LoginResponse;
import it.decimo.auth_service.services.AuthService;
import lombok.SneakyThrows;

@RestController
@RequestMapping(path = "/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login", produces = { "application/json" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LoginResponse.class)), description = "Il login è andato bene"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "Manca il JWT"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "Non è stato possibile effettuare il login"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "L'username contenuto nel JWT non esiste nel db"),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "JWT scaduto o formattatno male"), })
    public ResponseEntity<Object> login(@RequestHeader(value = "access-token", required = false) String jwt,
            @RequestBody(required = false) LoginBody body, HttpServletRequest request) {
        return authService.login(jwt, body);
    }

    @PostMapping(value = "/register", produces = { "application/json" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = LoginResponse.class)), description = "La registrazione è andata bene"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(implementation = BasicResponse.class)), description = "Non è stato possibile effettuare la registrazione"), })
    @SneakyThrows
    public ResponseEntity<Object> register(@RequestBody RegistrationDto body, HttpServletRequest request) {
        return authService.register(body);
    }
}
