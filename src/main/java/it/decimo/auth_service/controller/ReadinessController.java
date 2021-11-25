package it.decimo.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class ReadinessController {

    @GetMapping
    public ResponseEntity<Object> readiness() {
        return ResponseEntity.ok("Auth Service is up and running");
    }
}
