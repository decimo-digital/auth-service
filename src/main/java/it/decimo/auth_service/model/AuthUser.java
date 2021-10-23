package it.decimo.auth_service.model;

import java.util.Date;

public class AuthUser {
    private final int id;
    private final String secret;
    private final Date lastLogin;
    private final String password;

    public AuthUser(int id, String secret, Date lastLogin, String password) {
        this.id = id;
        this.secret = secret;
        this.lastLogin = lastLogin;
        this.password = password;
    }
}
