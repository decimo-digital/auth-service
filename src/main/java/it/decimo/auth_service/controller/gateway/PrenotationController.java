package it.decimo.auth_service.controller.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.decimo.auth_service.connector.PrenotationServiceConnector;
import it.decimo.auth_service.dto.PrenotationRequestDto;
import it.decimo.auth_service.dto.UserPrenotation;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/prenotation")
@CrossOrigin(origins = "*")
@NeedLogin
@Slf4j
public class PrenotationController {
    @Autowired
    private PrenotationServiceConnector prenotationServiceConnector;

    @PostMapping("/")
    public ResponseEntity<Object> createPrenotation(@RequestHeader("access-token") String jwt,
            @RequestBody PrenotationRequestDto prenotationRequest) {
        log.info("Sending prenotation request: {}", prenotationRequest.toString());
        return prenotationServiceConnector.makePrenotation(prenotationRequest);
    }

    @GetMapping("/{userId}")
    @ApiResponse(responseCode = "200", description = "Lista delle prenotazioni effettuate", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserPrenotation.class), minItems = 0, uniqueItems = true)))
    public ResponseEntity<Object> getUserPrenotations(@RequestHeader("access-token") String jwt,
            @PathVariable(name = "userId") int requesterId) {
        final var prenotations = prenotationServiceConnector.getPrenotations(requesterId);
        return ResponseEntity.ok().body(prenotations);
    }

}
