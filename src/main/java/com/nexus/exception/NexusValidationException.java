package com.nexus.exception;

public class NexusValidationException extends RuntimeException {
    private static int totalValidationErrors = 0;
    public NexusValidationException(String message) {
        super(message);
        totalValidationErrors++;
        // Dica para o aluno: Incrementar contador global de erros aqui? 
        // Ou melhor deixar para a Task gerenciar.
    }

    public int getTotalValidationErrors() { return totalValidationErrors; }
}
