package it.decimo.auth_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "auth_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(name = "auth_users_id_seq", sequenceName = "auth_users_id_seq")
public class AuthUser {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "google_id")
    private String googleId;
}
