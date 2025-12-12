package com.example.productapi.service;

import com.example.productapi.model.User;
import com.example.productapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Validates credentials against DB. Passwords are stored hashed (BCrypt optional).
     */
    public boolean validateCredentials(String username, String password) {
        log.info("🔍 AuthService.validateCredentials() llamado para usuario: {}", username);
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("❌ Usuario o password vacío");
            return false;
        }
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("❌ Usuario no encontrado en BD: {}", username);
            return false;
        }
        
        User user = userOpt.get();
        String stored = user.getPasswordHash();
        if (!StringUtils.hasText(stored)) {
            log.warn("❌ Password en BD está vacío o null para usuario: {}", username);
            return false;
        }
        log.info("✅ Usuario encontrado en BD. Password hash prefijo: {}...", stored.substring(0, Math.min(20, stored.length())));

        // If stored is a BCrypt hash, use BCrypt check; otherwise compare plain (not recommended)
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            try {
                boolean match = org.springframework.security.crypto.bcrypt.BCrypt.checkpw(password, stored);
                if (match) {
                    log.info("✅ Password BCrypt válido");
                } else {
                    log.warn("❌ Password BCrypt inválido");
                }
                return match;
            } catch (Throwable t) {
                log.error("❌ Error validando BCrypt", t);
                return false;
            }
        }
        
        boolean match = password.equals(stored);
        if (match) {
            log.info("✅ Password plano válido");
        } else {
            log.warn("❌ Password plano inválido (esperado: '{}', recibido: '{}')", stored, password);
        }
        return match;
    }
}
