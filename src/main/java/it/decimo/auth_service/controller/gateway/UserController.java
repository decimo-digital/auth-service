package it.decimo.auth_service.controller.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.decimo.auth_service.connector.UserServiceConnector;
import it.decimo.auth_service.dto.UserInfoDto;
import it.decimo.auth_service.dto.response.BasicResponse;
import it.decimo.auth_service.repository.UserRepository;
import it.decimo.auth_service.services.JwtUtils;
import it.decimo.auth_service.utils.annotations.NeedLogin;
import lombok.SneakyThrows;

@RestController
@NeedLogin
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserServiceConnector userServiceConnector;
    @Autowired
    private UserRepository userRepository;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ritorna le informazioni dell'utente", content = @Content(schema = @Schema(implementation = UserInfoDto.class))),
            @ApiResponse(responseCode = "404", description = "L'id inserito non punta a nessun utente esistente", content = @Content(schema = @Schema(implementation = BasicResponse.class))), })
    @GetMapping("/{id}")
    @SneakyThrows
    public ResponseEntity<Object> getUserInfo(@RequestHeader("access-token") String token,
            @PathVariable("id") String id) {
        int idToFind;
        try {
            idToFind = Integer.parseInt(id);
        } catch (Exception ignored) {
            final var email = ((String) jwtUtils.extractField(token, "username"));
            idToFind = userRepository.findByEmail(email).get().getId();
        }
        final var userInfo = userServiceConnector.getUserInfo(idToFind);
        if (userInfo == null) {
            return ResponseEntity.status(404)
                    .body(new BasicResponse("L'utente richiesto non è stato trovato", "USER_NOT_FOUND"));
        } else {
            return ResponseEntity.ok(userInfo);
        }
    }

}
