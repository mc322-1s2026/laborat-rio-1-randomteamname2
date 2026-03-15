package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.time.LocalDate;

public class Task {
    // Métricas Globais (Alunos implementam a lógica de incremento/decremento)
    public static int totalTasksCreated = 0;
    public static int totalValidationErrors = 0;
    public static int activeWorkload = 0;

    private static int nextId = 1;

    private int id;
    private LocalDate deadline; // Imutável após o nascimento
    private String title;
    private TaskStatus status;
    private User owner;
    private int estimatedEffort;

    public Task(String title, LocalDate deadline, int effort) {
        this.id = nextId++;
        this.deadline = deadline;
        this.title = title;
        this.status = TaskStatus.TO_DO;
        this.estimatedEffort = effort;
        
        // Ação do Aluno:
        totalTasksCreated++; 
    }

    /**
     * Move a tarefa para IN_PROGRESS.
     * Regra: Só é possível se houver um owner atribuído e não estiver BLOCKED.
     */
    public void moveToInProgrers() {
        // TODO: Implementar lógica de proteção e atualizar activeWorkload
        // Se falhar, incrementar totalValidationErrors e lançar NexusValidationException
        if (this.getStatus() == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("Task " + this.getTitle() + " está bloqueada."); 
        }

        if (this.getOwner() == null) {
            totalValidationErrors++;
            throw new NexusValidationException("Task " + this.getTitle() + " não contém usuário dono."); 
        }

        if (this.getStatus() == TaskStatus.IN_PROGRESS) return; 

        activeWorkload++;
        this.status = TaskStatus.IN_PROGRESS;
    }

    /**
     * Finaliza a tarefa.
     * Regra: Só pode ser movida para DONE se não estiver BLOCKED.
     */
    public void markAsDone() {
        // TODO: Implementar lógica de proteção e atualizar activeWorkload (decrementar)
        if (this.getStatus() == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("Task " + this.getTitle() + " está bloqueada."); 
        }

        if (this.getStatus() == TaskStatus.IN_PROGRESS) activeWorkload--;

        this.status = TaskStatus.DONE;
    }

    public void setBlocked() {
        if (this.getStatus() == TaskStatus.DONE) {
            totalValidationErrors++;
            throw new NexusValidationException("Task " + this.getTitle() + " está já está concluida e não pode ser bloqueada."); 
        }

        if (this.getStatus() == TaskStatus.IN_PROGRESS) activeWorkload--;

        this.status = TaskStatus.BLOCKED;
    }

    // Getters
    public int getId() { return id; }
    public TaskStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public LocalDate getDeadline() { return deadline; }
    public User getOwner() { return owner; }
    public int getEstimatedEffort() { return estimatedEffort; }
}
