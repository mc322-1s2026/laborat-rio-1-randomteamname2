package com.nexus.model;

public class User {
    private final String username;
    private final String email;

    public User(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }

        //Validação de email
        if (!email.contains("@") && !email.contains(".com") && !(email == null || email.isBlank())){
            throw new IllegalArgumentException("Email inválido");
        }

        this.username = username;
        this.email = email;
    }

    public String consultEmail() {
        return email;
    }

    public String consultUsername() {
        return username;
    }

    public long calculateWorkload() {
        consultUsername();
        return 0; 
    }
}