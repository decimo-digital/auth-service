package it.decimo.auth_service.utils.exception;

/**
 * Eccezione utilizzata nel {@link it.decimo.auth_service.services.JwtUtils}
 */
public class MissingKeyException extends Exception {
    public MissingKeyException(String message) {
        super(message);
    }
}
