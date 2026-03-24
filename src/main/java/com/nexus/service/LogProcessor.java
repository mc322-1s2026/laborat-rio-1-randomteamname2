package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

//TODO: fazer o parsing dos inputs e os switch cases
//TODO: tratamento de erro (completar as mudanças do arantes)

public class LogProcessor {

    public void processLog(String fileName, Workspace workspace, List<User> users) {
        try {
            // Busca o arquivo dentro da pasta de recursos do projeto (target/classes)
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
                            case "CREATE_USER" -> create_user(p[1], p[2], users);
                            case "CREATE_TASK" -> create_task(p[1], p[2], p[3], workspace);
                            case "CREATE_PROJECT" -> create_project(p[1], p[2], workspace);
                            case "ASSIGN_USER" -> assign_user(p[1], p[2]);
                            case "CHANGE_STATUS" -> change_status(p[1], p[2], p[3]);
                            case "REPORT_STATUS" -> report_status();
                            default -> System.err.println("[WARN] Ação desconhecida: " + action);
                        }
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
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
        boolean exists = workspace.getTasks().stream().filter(t->t.getTitle().equals(p1)).findFirst().isPresent(); 
        if(p1.isBlank() || p1.isEmpty() || exists){
            throw new NexusValidationException("Nome inválido");
        }
        try{
            deadline = LocalDate.parse(p2);
            effort = Integer.parseInt(p3);
        }
        catch(DateTimeParseException e){
            throw new NexusValidationException("Data inválida.");
        }
        catch(NumberFormatException e){
            throw new NexusValidationException("Tempo necessário inválido.");
        }
        
        Task t = new Task(p1, deadline, effort);
        workspace.addTask(t);
        System.out.println("[LOG] Tarefa criada: " + p1);
    }

    private void create_project(String p1, String p2, Workspace workspace){
        int budget;
        boolean exists = workspace.getProjects().stream().filter(t->t.getName().equals(p1)).findFirst().isPresent(); 
        if(p1.isBlank() || p1.isEmpty() || exists){
            throw new NexusValidationException("Nome inválido");
        }
        try{
            budget = Integer.parseInt(p2);
        }
        catch(NumberFormatException e){
            throw new NexusValidationException("Orçamento inválido.")
        }
        Project p = new Project(p1, budget);
        workspace.addProject(p);
        System.out.println("[LOG] Projeto criado: " + p1);
    }

    private void create_user(String p1, String p2, List<User> users){
        users.stream().filter()

        User u = new User(p1, p2);
        users.add(u);
        System.out.println("[LOG] Usuário criado: " + p1);
    }

    private void assign_user(String p1, String p2, List<User> users, Workspace workspace){
        int taskId;
        User u;
        try{
            taskId = Integer.parseInt(p2);
            users.stream().filter(u-> u.get)
        }
        catch(NumberFormatException e){
            throw new NexusValidationException("ID inválido");
        }
        workspace.getTaskById(taskId).setOwner(u)
    }
}