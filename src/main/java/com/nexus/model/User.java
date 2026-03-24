package com.nexus.model;

import com.nexus.service.Workspace;
import java.util.regex.Pattern;

public class User {
    private final String username;
    private final String email;

    public User(String username, String email) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }

        //Validação de email,
        if (!EmailValidator.isValid(email)){
            throw new IllegalArgumentException("Email inválido");
        }

        this.username = username;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public long calculateWorkload(Workspace workspace) {
        long countInProgress = workspace.getTasks().stream()
        .filter(p->p.getStatus().equals(TaskStatus.IN_PROGRESS) && p.getOwner().equals(this)).count();
        return countInProgress;
    }

    public class EmailValidator {
        private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        
        public static boolean isValid(String email){
            return Pattern.matches(EMAIL_REGEX, email);
        }
    }
}