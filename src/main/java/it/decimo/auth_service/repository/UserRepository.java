package it.decimo.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.decimo.auth_service.model.AuthUser;

@Repository
public interface UserRepository extends JpaRepository<AuthUser, Integer> {

    /**
     * Cerca una utenza con l'email e la password dati
     */
    Optional<AuthUser> findByEmailAndPassword(String email, String password);

    Optional<AuthUser> findByEmail(String email);

    void deleteByEmail(String email);
}
