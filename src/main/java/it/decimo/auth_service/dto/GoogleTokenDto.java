package it.decimo.auth_service.dto;

import lombok.Data;

@Data
public class GoogleTokenDto {
    private String googleId;
    private String firstName;
    private String lastName;
    private String email;
}
