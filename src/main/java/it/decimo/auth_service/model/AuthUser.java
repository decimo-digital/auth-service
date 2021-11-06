package it.decimo.auth_service.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "auth_users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;
    
    @Column(name = "last_login")
    private Date lastLogin;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;
}
