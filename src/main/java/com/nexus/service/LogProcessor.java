package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Processador de arquivos de log responsável por simular o uso do sistema.
 */
public class LogProcessor {

    /**
     * Lê um arquivo de log linha por linha, faz o parsing dos comandos e os executa, lidando com exceções para não interromper a execução do programa.
     * @param fileName      O nome do arquivo de log a ser lido (deve estar na pasta de resources).
     * @param workspace     O ambiente de trabalho atual onde as alterações ocorrerão.
     * @param users         A lista de usuários do sistema para registro e consulta.
     */
    public void processLog(String fileName, Workspace workspace, List<User> users) {

        try {
            var resource = getClass().getClassLoader().getResourceAsStream(fileName);
            
            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + fileName);
            }

            try (java.util.Scanner s = new java.util.Scanner(resource).useDelimiter("\\A")) {
                String content = s.hasNext() ? s.next() : "";
                List<String> lines = List.of(content.split("\\R"));
                
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] p = line.split(";");
                    String action = p[0];

                    try {
                        switch (action) {
                            case "CREATE_USER"      -> create_user(p[1], p[2], users);
                            case "CREATE_TASK"      -> create_task(p[1], p[2], p[3], workspace);
                            case "CREATE_PROJECT"   -> create_project(p[1], p[2], workspace);
                            case "ASSIGN_USER"      -> assign_user(p[1], p[2], users, workspace);
                            case "CHANGE_STATUS"    -> change_status(p[1], p[2], workspace);
                            case "REPORT_STATUS"    -> report_status(workspace);
                            default -> System.err.println("[WARN] Ação desconhecida: " + action);
                        }
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.err.println("[ERRO DE PARÂMETRO] Parâmetro inválido no comando '" + line + "': " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }

    private void create_task(String p1, String p2, String p3, Workspace workspace){
        LocalDate deadline;
        int effort;
        boolean exists = workspace.getTasks().stream()
                            .filter(t->t.getTitle().equals(p1))
                            .findFirst()
                            .isPresent(); 

        if (p1.isBlank() || p1.isEmpty() || exists) {
            throw new IllegalArgumentException("Nome inválido");
        }

        try {
            deadline = LocalDate.parse(p2);
            effort = Integer.parseInt(p3);
        } catch(DateTimeParseException e){
            throw new IllegalArgumentException("Data inválida.");
        } catch(NumberFormatException e){
            throw new IllegalArgumentException("Tempo necessário inválido.");
        }
        
        Task t = new Task(p1, deadline, effort);
        workspace.addTask(t);
        System.out.println("[LOG] Tarefa criada: " + p1);
    }

    private void create_project(String p1, String p2, Workspace workspace){
        int budget;
        boolean exists = workspace.getProjects().stream()
                            .filter(t->t.getName().equals(p1))
                            .findFirst()
                            .isPresent(); 

        if (p1.isBlank() || p1.isEmpty() || exists){
            throw new IllegalArgumentException("Nome inválido");
        }
        try {
            budget = Integer.parseInt(p2);
        } catch(NumberFormatException e){
            throw new IllegalArgumentException("Tempo máximo inválido.");
        }
        Project p = new Project(p1, budget);
        workspace.addProject(p);
        System.out.println("[LOG] Projeto criado: " + p1);
    }

    private void create_user(String p1, String p2, List<User> users){
        if (users.stream().filter(u -> u.getUsername().equals(p1)).findFirst().isPresent()) {
            throw new NexusValidationException("O usuário já existe.");
        }

        User u = new User(p1, p2);
        users.add(u);
        System.out.println("[LOG] Usuário criado: " + p1);
    }

    private void assign_user(String p1, String p2, List<User> users, Workspace workspace){
        int taskId;
        User u;
        try {
            taskId = Integer.parseInt(p1);
            u = users.stream().filter(u_->u_.getUsername().equals(p2)).findFirst().get();
        } catch(NoSuchElementException e){
            throw new IllegalArgumentException("Usuário não existe.");
        } catch(NumberFormatException e){
            throw new IllegalArgumentException("ID inválido");
        }

        Task task = workspace.getTaskById(taskId);
        task.setOwner(u);
        System.out.println("[LOG] Usuário " + p2 + " atribuído como dono da tarefa " + task.getTitle());
    }

    private void change_status(String p1, String p2, Workspace workspace){
        int taskId;
        try {
            taskId = Integer.parseInt(p1);
        } catch(NumberFormatException e){
            throw new IllegalArgumentException("ID inválido.");
        }

        Task t = workspace.getTaskById(taskId);
        switch (p2) {
            case "IN_PROGRESS":
                t.moveToInProgrers();
                break;
            case "DONE":
                t.markAsDone();
                break;
            case "BLOCKED":
                t.setBlocked();
                break;
            case "TO_DO":
                throw new IllegalArgumentException("Não possível alterar o estado da tarefa para TO_DO.");
            default:
                throw new IllegalArgumentException("Status inválido.");
        }
        System.out.println("[LOG] Status da Tarefa " + t.getTitle() + " alterado para " + p2 + " com sucesso.");
    } 

    private void report_status(Workspace workspace){
        System.out.println("");
        String header = "+----------------------------------------------+";
        System.out.println(header);
        System.out.println("Os 3 melhores usuários são: ");
        workspace.topPerformers().stream()
            .map(u -> u.getUsername())
            .forEach(System.out::println);
        
        //Overloaded Users
        System.out.println(header);
        System.out.println("Os usuários sobrecarregados são: ");
        workspace.overloadedUsers().stream()
            .map(u -> u.getUsername())
            .forEach(System.out::println);

        //Bottleneck
        System.out.println(header);
        List<TaskStatus> l = workspace.bottleneck();
        if (l != null) {
            System.out.println("O status com mais tarefas é: ");
            l.stream().forEach(System.out::println);
        } else {
            System.out.println("Não existem tarefas que não estejam concluídas.");
        }
        System.out.println(header);
        System.out.println("");
    }
}
