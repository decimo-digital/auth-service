package it.decimo.auth_service.repository;

import it.decimo.auth_service.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AuthUser, Integer> {

    /**
     * Cerca una utenza con l'email e la password dati
     */
    Optional<AuthUser> findByEmailAndPassword(String email, String password);

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByGoogleId(String googleId);

    Optional<AuthUser> findByEmailOrGoogleId(String email, String googleId);
    
    void deleteByEmail(String email);
    
    @Query(value = "SELECT max(id) FROM auth_user")
    int getCurrentMaxId();
}
