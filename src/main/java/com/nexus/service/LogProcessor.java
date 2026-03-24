package com.nexus.service;

import com.nexus.model.*;
import com.nexus.exception.NexusValidationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

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
                            case "CREATE_USER" -> {
                                users.add(new User(p[1], p[2]));
                                System.out.println("[LOG] Usuário criado: " + p[1]);
                            }
                            case "CREATE_TASK" -> create_task(p[1], p[2], p[3], workspace);
                            case "CREATE_PROJECT" -> create_project(p[1], p[2], workspace);
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
        boolean exists = workspace.getTasks().stream().filter(t->t.getTitle().equals(p1)).findFirst().isPresent(); 
        if(p1.isBlank() || p1.isEmpty() || exists){
            throw new NexusValidationException("Nome inválido");
        }
        
        Task t = new Task(p1, deadline, effort);
        workspace.addTask(t);
        System.out.println("[LOG] Tarefa criada: " + p1);
    }

    private void create_project(String p1, String p2, Workspace workspace){
        boolean exists = workspace.getTasks().stream().filter(t->t.getTitle().equals(p1)).findFirst().isPresent(); 
        if(p1.isBlank() || p1.isEmpty() || exists){
            throw new NexusValidationException("Nome inválido");
        }

        
    }
}