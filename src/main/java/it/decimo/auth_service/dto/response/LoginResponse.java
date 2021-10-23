package it.decimo.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
