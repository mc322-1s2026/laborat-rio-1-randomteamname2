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

    public Project(String name, int totalBudget) {
        this.name = name;
        this.currentEffort = 0;
        this.totalBudget = totalBudget;
    }

    public void addTask(Task t) {
        if (t.getEstimatedEffort() + currentEffort > totalBudget) {
            throw new NexusValidationException("A tarefa " + t.getTitle() + " não pode ser adicionada ao projeto " + name);
        }

        currentEffort += t.getEstimatedEffort();
        tasks.add(t);
         
    }

    public double getProjectHealth() {
        List<Task> _tasks = getTasks();
        long totalTasks = _tasks.size();
        long doneTasks = _tasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.DONE)
            .count();
        return doneTasks / totalTasks;
    }

    public String getName() { return name; }
    public List<Task> getTasks() { return Collections.unmodifiableList(tasks); }
    public int getTotalBudget() { return totalBudget; }
    public int getCurrentEffort() { return currentEffort; }
}
