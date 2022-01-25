package it.decimo.auth_service.controller.gateway;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.decimo.auth_service.connector.MerchantServiceConnector;
import it.decimo.auth_service.dto.Merchant;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.services.AuthService;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @ApiResponse(responseCode = "200", description = "Ritorna la lista di esercenti disponibili. Opzionalmente ordinata", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Merchant.class), minItems = 0, uniqueItems = true)))})
    public ResponseEntity<Object> findAll(@RequestHeader(value = "access-token", required = false) String jwt) {
        final var merchants = merchantServiceConnector.getMerchants();
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
            @ApiResponse(responseCode = "200", description = "Il merchant è stato salvato", content = @Content(schema = @Schema(implementation = Merchant.class))),
            @ApiResponse(responseCode = "500", description = "Per qualche problema non ha salvato il merchant", content = @Content(schema = @Schema(implementation = BasicResponse.class)))
    })
    @SneakyThrows
    public ResponseEntity<Object> saveItem(@RequestHeader(value = "access-token", required = false) String jwt,
                                           @RequestBody Merchant merchant) {
        final var id = authService.getIdFromJwt(jwt);
        merchant.setOwner(id);
        merchant.setEnabled(true);

        return merchantServiceConnector.saveMerchant(merchant);
    }

    @PatchMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Il merchant è stato aggiornato", content = @Content(schema = @Schema(implementation = Merchant.class))),
            @ApiResponse(responseCode = "404", description = "Il merchant richiesto non esiste")
    })
    public ResponseEntity<Object> patchMerchantData(
            @RequestHeader(value = "access-token", required = false) String jwt, @PathVariable int id,
            @RequestBody Merchant update) {
        final var requesterId = authService.getIdFromJwt(jwt);
        log.info("User {} is updating merchant {}", requesterId, id);

        return merchantServiceConnector.updateMerchantData(id, update);
    }

    @GetMapping("/{id}/data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "I dati del merchant richiesto", content = @Content(schema = @Schema(implementation = Merchant.class))),
            @ApiResponse(responseCode = "404", description = "Il merchant richiesto non esiste")})
    public ResponseEntity<Object> getMerchantData(@RequestHeader(value = "access-token", required = false) String jwt,
                                                  @PathVariable int id) {
        final var merchant = merchantServiceConnector.getMerchant(id);
        log.info("Received merchant data for merchant with id {}", id);
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(merchant);
    }

    @DeleteMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Il merchant è stato cancellato"),
            @ApiResponse(responseCode = "404", description = "Il merchant richiesto non esiste")
    })
    public ResponseEntity<Object> deleteMerchant(@RequestHeader(value = "access-token", required = false) String jwt,
                                                 @PathVariable int id) {
        final var requesterId = authService.getIdFromJwt(jwt);
        final var deleted = merchantServiceConnector.deleteMerchant(id, requesterId);
        log.info("Deleted merchant with id {}: {}", id, deleted);
        if (!deleted) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}
