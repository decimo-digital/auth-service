package it.decimo.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Il DTO utilizzato in {@link it.decimo.auth_service.controller.AuthController} per effettuare la login
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginBody {

    private String username;
    private String password;

    @Override
    public String toString() {
        return String.format("LoginBody [ %s, %s]", username, password);
    }
}
