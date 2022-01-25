package it.decimo.auth_service.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "auth_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "auth_users_id_seq", sequenceName = "auth_users_id_seq")
public class AuthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_users_id_seq")
    @Column(name = "id", updatable = false, nullable = false)
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
