package com.nexus.model;

import com.nexus.exception.NexusValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Project {
    private String name;
    private List<Task> tasks = new ArrayList<Task>(); 
    private int totalBudget;
    private int currentEffort;

    /**
     * Construtor da classe Project.
     * @param name          Nome do Projeto.
     * @param totalBudget   Tempo máximo a ser gasto no Projeto.
     */
    public Project(String name, int totalBudget) {
        this.name = name;
        this.currentEffort = 0;
        this.totalBudget = totalBudget;
    }

    /**
     * Adiciona uma Tarefa ao Projeto.
     * @param t Tarefa que se quer adicioanr ao Projeto.
     */
    public void addTask(Task t) {
        if (t.getEstimatedEffort() + currentEffort > totalBudget) {
            throw new NexusValidationException("A tarefa " + t.getTitle() + " não pode ser adicionada ao projeto " + name);
        }

        currentEffort += t.getEstimatedEffort();
        tasks.add(t);
         
    }

    /**
     * @returns O percentual de Tarefas concluídas no projeto.
     */
    public double getProjectHealth() {
        List<Task> _tasks = getTasks();
        long totalTasks = _tasks.size();
        long doneTasks = _tasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.DONE)
            .count();
        return doneTasks / totalTasks;
    }

    // Getters
    public String getName() { return name; }
    public List<Task> getTasks() { return Collections.unmodifiableList(tasks); }
    public int getTotalBudget() { return totalBudget; }
    public int getCurrentEffort() { return currentEffort; }
}
