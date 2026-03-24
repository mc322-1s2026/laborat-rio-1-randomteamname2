package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Workspace {
    private final List<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        // Retorna uma visão não modificável para garantir encapsulamento
        return Collections.unmodifiableList(tasks);
    }

    public Task getTaskById(int id) {
        if (id < 1 || id > tasks.size()) 
        throw new NexusValidationException("ID " + id + " inválido."); 

        return getTasks().get(id - 1);
    }

    public List<User> topPerformers() {
        return getTasks().stream()
            .filter(task -> task.getStatus().equals(TaskStatus.DONE))
            .collect(Collectors.groupingBy(Task::getOwner, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(3)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }

    public List<User> overloadedUsers() {
        return getTasks().stream()
            .filter(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS))
            .collect(Collectors.groupingBy(Task::getOwner, Collectors.counting()))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 10)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }


    public List<TaskStatus> bottleneck() {
        Long bottleneckSize = getTasks().stream()
            .filter(task -> !task.getStatus().equals(TaskStatus.DONE))
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
            .entrySet()
            .stream()
            .mapToLong(entry -> entry.getValue())
            .max()
            .orElse(0);

        if (bottleneckSize == 0) return null;

        return getTasks().stream()
            .filter(task -> task.getStatus().equals(TaskStatus.DONE))
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(bottleneckSize))
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }
}
