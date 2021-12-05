package it.decimo.auth_service.controller.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.decimo.auth_service.connector.PrenotationServiceConnector;
import it.decimo.auth_service.dto.Prenotation;
import it.decimo.auth_service.dto.PrenotationRequestDto;
import it.decimo.auth_service.dto.UserPrenotation;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.services.AuthService;
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
    @Autowired
    private AuthService authService;

    @PostMapping()
    public ResponseEntity<Object> createPrenotation(@RequestHeader("access-token") String jwt,
            @RequestBody PrenotationRequestDto prenotationRequest) {
        log.info("Sending prenotation request: {}", prenotationRequest.toString());
        return prenotationServiceConnector.makePrenotation(prenotationRequest);
    }

    @GetMapping()
    @ApiResponse(responseCode = "200", description = "Lista delle prenotazioni effettuate", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserPrenotation.class), minItems = 0, uniqueItems = true)))
    public ResponseEntity<Object> getUserPrenotations(@RequestHeader("access-token") String jwt,
            @RequestParam(name = "userId", required = false) Integer requesterId) {
        if (requesterId == null) {
            requesterId = authService.getIdFromJwt(jwt);
        }
        final var prenotations = prenotationServiceConnector.getPrenotations(requesterId);

        if (prenotations.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(prenotations.getBody());
        }

        return ResponseEntity.status(prenotations.getStatusCode()).body(prenotations.getBody());
    }

    @GetMapping("/{merchantId}/prenotations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista delle prenotazioni effettuate", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Prenotation.class), minItems = 0, uniqueItems = true))),
            @ApiResponse(responseCode = "404", description = "Non è stato trovato il locale richiesto", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "401", description = "L'utente richiedente non ha i permessi necessari per la risorsa", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
    })
    public ResponseEntity<Object> getPrenotations(@PathVariable(name = "merchantId") int merchantId,
            @RequestHeader("access-token") String jwt) {
        int requesterId = authService.getIdFromJwt(jwt);
        final var response = prenotationServiceConnector.getPrenotationsForMerchant(merchantId, requesterId);
        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        }

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/{prenotationId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "L'utente specificato è stato aggiunto alla prenotazione", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "401", description = "L'utente richiedente non è l'owner della prenotazione e non può aggiungere gente", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "404", description = "Non è stata trovata nessuna prenotazione esistente", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "422", description = "L'utente era già stato registrato nella prenotazione", content = @Content(schema = @Schema(implementation = BasicResponse.class))) })
    public ResponseEntity<Object> addUserToPrenotation(@RequestHeader("access-token") String jwt,
            @Param(value = "prenotationId") int prenotationId, @PathVariable int userId) {
        try {
            final var requesterId = authService.getIdFromJwt(jwt);
            return prenotationServiceConnector.addUserToPrenotation(requesterId, prenotationId, userId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "La prenotazione è stata modificata con successo", content = @Content(schema = @Schema(implementation = Prenotation.class))),
            @ApiResponse(responseCode = "404", description = "La prenotazione non esiste", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "401", description = "L'utente non può modificare la prenotazione", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
    })
    public ResponseEntity<Object> editPrenotation(@RequestHeader("access-token") String jwt,
            @RequestBody Prenotation prenotation) {
        final var requesterId = authService.getIdFromJwt(jwt);
        try {
            return prenotationServiceConnector.updatePrenotation(requesterId, prenotation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
