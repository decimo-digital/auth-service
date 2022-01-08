package it.decimo.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
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
    
    private boolean isMerchant;
}
