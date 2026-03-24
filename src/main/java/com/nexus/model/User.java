package com.nexus.model;

import com.nexus.service.Workspace;
import java.util.regex.Pattern;

public class User {
    private final String username;
    private final String email;
    
    /**
     * Construtor da classe User.
     * @param username      O nome de usuário. Não pode ser nulo ou conter apenas espaços.
     * @param email         O endereço de e-mail do usuário. Deve ser um e-mail válido.
     * @throws IllegalArgumentException se o username for vazio/nulo ou se o e-mail for inválido.
     */
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
    
    public String getEmail() { return email; }
    public String getUsername() { return username; }

    /**
     * Calcula a carga de trabalho atual do usuário com base no Workspace fornecido.
     * A carga de trabalho é definida pela quantidade de tarefas atribuídas a este usuário que estão atualmente no status IN_PROGRESS.
     * @param workspace O Workspace que contém a lista global de tarefas.
     * @return A carga de trabalho atual do usuário.
     */
    public long calculateWorkload(Workspace workspace) {
        long countInProgress = workspace.getTasks().stream()
        .filter(p->p.getStatus().equals(TaskStatus.IN_PROGRESS) && p.getOwner().equals(this)).count();
        return countInProgress;
    }

    private class EmailValidator {
        private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        
        public static boolean isValid(String email){
            return Pattern.matches(EMAIL_REGEX, email);
        }
    }
}
