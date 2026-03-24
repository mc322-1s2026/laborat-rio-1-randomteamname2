package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.time.LocalDate;

public class Task {
    // Métricas Globais (Alunos implementam a lógica de incremento/decremento)
    public static int totalTasksCreated = 0;
    public static int activeWorkload = 0;

    private static int nextId = 1;

    private final int id;
    private final LocalDate deadline;
    private String title;
    private TaskStatus status;
    private User owner;
    private int estimatedEffort;

    /**
     * Constutor da classe Task;
     * @param title         Título da tarefa
     * @param deadline      Prazo de entrega da tarefa
     * @param effort        Tempo estimado a ser gasto na tarefa
     */
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
     * Só é possível se houver um owner atribuído e não estiver BLOCKED.
     * @throws NexusValidationException Joga uma excessão se a tarefa não puder mudar para IN_PROGRESS
     */
    public void moveToInProgrers() {
        
        // Se falhar, incrementar totalValidationErrors e lançar NexusValidationException
        if (getStatus().equals(TaskStatus.BLOCKED)) {
            throw new NexusValidationException("Task " + getTitle() + " está bloqueada."); 
        }

        if (getOwner() == null) {
            throw new NexusValidationException("Task " + getTitle() + " não contém usuário dono."); 
        }

        if (getStatus().equals(TaskStatus.IN_PROGRESS)) return; 

        activeWorkload++;
        status = TaskStatus.IN_PROGRESS;

    }

    /**
     * Finaliza a tarefa.
     * Regra: Só pode ser movida para DONE se não estiver BLOCKED.
     * @throws NexusValidationException Joga uma excessão se a tarefa estiver bloqueada.
     */
    public void markAsDone() {
        
        if (getStatus().equals(TaskStatus.BLOCKED)) {
            throw new NexusValidationException("Task " + getTitle() + " está bloqueada."); 
        }

        if (getStatus().equals(TaskStatus.IN_PROGRESS)) activeWorkload--;

        status = TaskStatus.DONE;
    }

    /**
     * Bloqueia a tarefa.
     * Regra: Só pode ser movida para BLOCKED se não estiver DONE.
     * @throws NexusValidationException Joga uma excessão se a tarefa estiver concluída.
     * Se a tarefa a ser bloqueada estiver em progresso, diminui o activeWorkload.
     */
    public void setBlocked() {
        if (getStatus().equals(TaskStatus.DONE)) {
            throw new NexusValidationException("Task " + getTitle() + " está já está concluida e não pode ser bloqueada."); 
        }

        if (getStatus().equals(TaskStatus.IN_PROGRESS)) activeWorkload--;

        status = TaskStatus.BLOCKED;

    }

    // Getters
    public int getId() { return id; }
    public TaskStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public LocalDate getDeadline() { return deadline; }
    public User getOwner() { return owner; }
    public int getEstimatedEffort() { return estimatedEffort; }

    public void setOwner(User user) { owner = user; }
}
