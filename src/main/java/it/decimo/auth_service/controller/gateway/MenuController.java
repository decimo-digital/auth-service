package it.decimo.auth_service.controller.gateway;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.decimo.auth_service.connector.MenuConnector;
import it.decimo.auth_service.dto.MenuCategory;
import it.decimo.auth_service.dto.MenuItem;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.services.AuthService;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/menu")
@NeedLogin
@CrossOrigin(origins = "*")
public class MenuController {

    @Autowired
    private MenuConnector menuConnector;
    @Autowired
    private AuthService authService;


    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ritorna la lista di elementi che compongono il menu del locale", content = @Content(array = @ArraySchema(minItems = 0, uniqueItems = true, schema = @Schema(implementation = MenuItem.class)))), @ApiResponse(responseCode = "404", description = "Il ristorante ricercato non esite", content = @Content(schema = @Schema(implementation = BasicResponse.class)))})
    @GetMapping("/{id}")
    public ResponseEntity<Object> getMenu(@PathVariable int id, @RequestHeader(value = "access-token") String token) {
        log.info("Requesting menu of merchant {}", id);
        try {
            return ResponseEntity.ok(menuConnector.getMenu(id));
        } catch (Exception e) {
            log.error("Failed to send request to getMenu", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ritorna la lista di categorie per i piatti del menu", content = @Content(array = @ArraySchema(minItems = 0, uniqueItems = true, schema = @Schema(implementation = MenuCategory.class))))
    })
    public List<MenuCategory> getCategories() {
        return menuConnector.getCategories();
    }

    @PatchMapping("/{merchantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ritorna la copia dell'oggetto modificato correttamente", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "404", description = "Il ristorante ricercato non esite", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "404", description = "L'oggetto richiesto non esiste", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "401", description = "L'utente che ha richiesto l'update non è autorizzato", content = @Content(schema = @Schema(implementation = BasicResponse.class)))
    })
    public ResponseEntity<Object> updateMenuItem(@PathVariable int merchantId, @RequestBody MenuItem item, @RequestHeader(value = "access-token") String token) {
        try {
            final var requesterId = authService.getIdFromJwt(token);
            log.info("User {} is updating element in menu to merchant {}", requesterId, merchantId);
            return menuConnector.updateMenuItem(merchantId, item, requesterId);
        } catch (Exception e) {
            log.error("Failed to update menu item", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ritorna l'id dell'oggetto che è stato inserito", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "404", description = "Il ristorante ricercato non esite", content = @Content(schema = @Schema(implementation = BasicResponse.class)))})
    @PostMapping("/{id}")
    public ResponseEntity<Object> insertItem(@PathVariable int id, @RequestBody MenuItem item, @RequestHeader(value = "access-token") String token) {
        try {
            final var requesterId = authService.getIdFromJwt(token);
            log.info("Inserting element in menu to merchant {}", id);
            return menuConnector.insertItem(id, item, requesterId);
        } catch (Exception e) {
            log.error("Failed to insert item in menu", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ha rimosso l'oggetto dal menu del locale", content = @Content(schema = @Schema(implementation = BasicResponse.class))),
            @ApiResponse(responseCode = "404", description = "Il locale non è stato trovato", content = @Content(schema = @Schema(implementation = BasicResponse.class)))})

    @DeleteMapping("/{merchantId}/{itemId}")
    public ResponseEntity<Object> deleteMenuItem(@PathVariable(name = "merchantId") int merchantId,
                                                 @PathVariable(name = "itemId") int itemId, @RequestHeader(value = "access-token") String token) {
        try {
            final var requesterId = authService.getIdFromJwt(token);
            menuConnector.deleteMenuItem(merchantId, itemId, requesterId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete menu item", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
