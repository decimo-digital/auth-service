package it.decimo.auth_service.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "auth_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {
    @Id
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private int id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Transient
    private boolean isMerchant;
}
