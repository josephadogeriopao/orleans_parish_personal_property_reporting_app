package com.opao.pp_api.services;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class ArgonService {

    private final PasswordEncoder passwordEncoder;

    // Spring clean constructor injection automatically maps the PasswordEncoder configuration bean
    public ArgonService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * GENERATE PASSWORD HASH (One-way)
     * Takes raw text and turns it into a secure, random-salted Argon2 string.
     */
    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password string cannot be empty");
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * VERIFY PASSWORD (Replaces Decryption)
     * Compares raw user input against the cryptographic hash stored inside the database.
     */
    public boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        // Internally extracts salt/parameters from the stored string to accurately evaluate a match
        return passwordEncoder.matches(rawPassword, storedHash);
    }
}