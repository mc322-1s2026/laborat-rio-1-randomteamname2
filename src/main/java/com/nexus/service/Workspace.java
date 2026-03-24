package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gerencia as listas globais de Tasks e Projects e possui alguns métodos de consulta a essas listas.
 */
public class Workspace {
    private final List<Task> tasks = new ArrayList<>();
    private final List<Project> projects = new ArrayList<>();

    /**
     * Adiciona uma Tarefa ao Workspace.
     * @param task Tarefa que se quer adicionar ao Workspace.
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Adiciona um Projeto ao Workspace.
     * @param project Projeto que se quer adicionar ao Workspace.
     */
    public void addProject(Project project) {
        projects.add(project);
    }

    /**
     * @returns Uma lista com todas as Tarefas do Workspace.
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * @returns Uma lista com todas os Projetos do Workspace.
     */
    public List<Project> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    /**
     * @param id O ID de uma tarefa.
     * @throws NexusValidationException Joga uma excessão se não existe uma tarefa com o ID em questão.
     * @returns A tarefa com ID correspondente.
     */
    public Task getTaskById(int id) {
        if (id < 1 || id > tasks.size()) {
            throw new NexusValidationException("ID " + id + " inválido."); 
        }

        return getTasks().get(id - 1);
    }

    /**
     * @returns Uma lista com os 3 usuários com mais tarefas completas.
     */
    public List<User> topPerformers() {
        return getTasks().stream()
            .filter(task -> task.getStatus().equals(TaskStatus.DONE) && task.getOwner() != null)
            .collect(Collectors.groupingBy(Task::getOwner, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(3)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }

    /**
     * @returns uma lista com todos usuários que possuem ao menos 10 tarefas em progresso.
     */
    public List<User> overloadedUsers() {
        return getTasks().stream()
            .filter(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS) && task.getOwner() != null)
            .collect(Collectors.groupingBy(Task::getOwner, Collectors.counting()))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 10)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }

    /**
     * Retorna uma lista com os status que possuem a maior quantidade de tarefas.
     * Repare que o método não retorna um único elemento para evitar problemas quando dois status possuem a mesma quantidade de tarefas.
     * @throws NexusValidationException Joga uma excessão caso não existam tarefas que não estejam concluídas.
     */
    public List<TaskStatus> bottleneck() {
        Long bottleneckSize = getTasks().stream()
            .filter(task -> !task.getStatus().equals(TaskStatus.DONE))
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
            .entrySet()
            .stream()
            .mapToLong(entry -> entry.getValue())
            .max()
            .orElse(0);

        return getTasks().stream()
            .filter(task -> !task.getStatus().equals(TaskStatus.DONE))
            .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(bottleneckSize))
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }
}
