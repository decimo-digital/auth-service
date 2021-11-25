package it.decimo.auth_service.controller.gateway;

import org.springframework.beans.factory.annotation.Autowired;
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
import it.decimo.auth_service.connector.MerchantServiceConnector;
import it.decimo.auth_service.dto.Merchant;
import it.decimo.auth_service.dto.MerchantDto;
import it.decimo.auth_service.dto.MerchantStatusDto;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.services.AuthService;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NeedLogin
@RestController
@RequestMapping(value = "/api/merchant")
@CrossOrigin(origins = "*")
public class MerchantController {
    @Autowired
    private AuthService authService;
    @Autowired
    private MerchantServiceConnector merchantServiceConnector;

    @GetMapping(produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ritorna la lista di esercenti disponibili. Opzionalmente ordinata", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Merchant.class), minItems = 0, uniqueItems = true))) })
    public ResponseEntity<Object> findAll(@RequestHeader(value = "access-token", required = false) String jwt,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng) {
        final var merchants = merchantServiceConnector.getMerchants(lat, lng);
        log.info("Received {} merchants from merchant_service", merchants.size());
        try {
            return ResponseEntity.ok().body(merchants);
        } catch (Exception e) {
            log.error("Error while building response", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value = "/", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Il merchant è stato salvato", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "500", description = "Per qualche problema non ha salvato il merchant", content = @Content(schema = @Schema(implementation = BasicResponse.class))) })
    @SneakyThrows
    public ResponseEntity<Object> saveItem(@RequestHeader(value = "access-token", required = false) String jwt,
            @RequestBody Merchant merchant) {
        final var id = authService.getIdFromJwt(jwt);
        merchant.setOwner(id);

        final var saved = merchantServiceConnector.saveMerchant(merchant);
        if (!saved) {
            log.error("Failed to save merchant");
            return ResponseEntity.internalServerError()
                    .body(new BasicResponse("C'è stato qualche errore a salvare il merchant", "GENERIC_ERROR"));
        } else {
            log.info("Saved merchant");
            return ResponseEntity.ok().body(BasicResponse.builder().code("OK").build());
        }
    }

    @PatchMapping("/{id}")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Il merchant è stato aggiornato"),
            @ApiResponse(responseCode = "404", description = "Il merchant richiesto non esiste") })
    public ResponseEntity<Object> patchMerchantStatus(
            @RequestHeader(value = "access-token", required = false) String jwt, @PathVariable int id,
            @RequestBody MerchantStatusDto update) {
        final var updated = merchantServiceConnector.updateMerchant(id, update);
        log.info("Updated merchant with id {}: {}", id, updated);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "I dati del merchant richiesto", content = @Content(schema = @Schema(implementation = MerchantDto.class))),
            @ApiResponse(responseCode = "404", description = "Il merchant richiesto non esiste") })
    public ResponseEntity<Object> getMerchantData(@RequestHeader(value = "access-token", required = false) String jwt,
            @PathVariable int id) {
        final var merchant = merchantServiceConnector.getMerchant(id);
        log.info("Received merchant data for merchant with id {}", id);
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(merchant);
    }
}
