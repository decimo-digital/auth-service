package it.decimo.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserInfoDto {
    private int id;
    @JsonAlias(value = "first_name")
    private String firstName;
    @JsonAlias(value = "last_name")
    private String lastName;
    private String email;
    private String phone;
    /**
     * Base64 dell'immagine di profilo
     */
    private String propic;
    /**
     * Il codice di referral univoco dell'utente
     */
    private String referral;
}
