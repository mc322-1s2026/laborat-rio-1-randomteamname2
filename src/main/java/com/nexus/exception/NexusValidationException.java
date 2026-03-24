package com.nexus.exception;

public class NexusValidationException extends RuntimeException {
    private static int totalValidationErrors = 0;

    /**
     * Construtor da classe NexusValidationException, recebe uma mensagem a ser impressa na mensagem de erro.
     * Realiza a contagem do total de erros lançados durante a execução do projeto.
     */
    public NexusValidationException(String message) {
        super(message);
        totalValidationErrors++;
    }

    // Getters
    public static int getTotalValidationErrors() { return totalValidationErrors; }
}
