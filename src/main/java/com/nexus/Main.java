package com.nexus;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.nexus.exception.NexusValidationException;
import com.nexus.model.Task;
import com.nexus.model.User;
import com.nexus.service.LogProcessor;
import com.nexus.service.Workspace;

/**
 * Ponto de entrada para a aplicação Nexus.
 * *
 * Esta classe fornece uma interface simples baseada em console usada no
 * trabalho de laboratório. Gerencia uma coleção de {@link User usuários}
 * e um {@link Workspace} onde as {@link Task tarefas} são armazenadas. As
 * operações são realizadas através de um laço de menu e delegadas a métodos
 * auxiliares.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Workspace workspace = new Workspace();
    private static final List<User> users = new ArrayList<>();
    private static final LogProcessor logProcessor = new LogProcessor();

    /**
     * Inicia a aplicação e processa comandos do usuário até a terminação.
     *
     * @param args argumentos de linha de comando (ignorados)
     */
    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            displayMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "0" -> {
                    System.out.println("Encerrando Nexus Motor...");
                    running = false;
                }
                case "1" -> addUser();
                case "2" -> addTask();
                case "3" -> listTasks();
                case "4" -> {
                    System.out.println("1. Carregar Log V1 (Básico)\n2. Carregar Log V2 (Desafio)");
                    String logChoice = scanner.nextLine();
                    String file = (logChoice.equals("1")) ? "log_v1.txt" : "log_v2.txt";
                    logProcessor.processLog(file, workspace, users);
                }
                default -> System.out.println("\n[!] Opção inválida.");
            }
        }
    }

    /**
     * Imprime o menu principal na saída padrão.
     * <p>As opções do menu correspondem às escolhas tratadas em
     * {@link #main(String[])}.
     * </p>
     */
    private static void displayMenu() {
        System.out.print("""
            
            ======= NEXUS CORE: MENU =======
            1. Adicionar Usuário
            2. Adicionar Tarefa
            3. Listar Todas as Tarefas
            4. Processar Log de Ações
            0. Sair
            Escolha uma opção:\s""");
    }

    /**
     * Solicita ao usuário nome de usuário e email, cria um novo {@link User} e
     * adiciona-o à lista interna. Exceções de validação são relatadas no
     * fluxo de erro.
     */
    private static void addUser() {
        try {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();

            User newUser = new User(username, email);
            users.add(newUser);
            System.out.println("[OK] Usuário " + username + " cadastrado.");
        } catch (NexusValidationException e) {
            System.err.println("[ERRO] " + e.getMessage());
        }
    }

    /**
     * Coleta detalhes da tarefa do usuário, constrói uma {@link Task} e
     * a acrescenta ao workspace. Erros de parsing de data são informados no
     * stderr.
     */
    private static void addTask() {
        try {
            System.out.print("Título da Tarefa: ");
            String title = scanner.nextLine();
            System.out.print("Prazo (AAAA-MM-DD): ");
            LocalDate deadline = LocalDate.parse(scanner.nextLine());
            System.out.print("Tempo necessário (em horas): ");
            int estimatedEffort = Integer.parseInt(scanner.nextLine());

            Task newTask = new Task(title, deadline, estimatedEffort);
            workspace.addTask(newTask);
            System.out.println("[OK] Tarefa " + title + " adicionada ao backlog.");
        } catch (DateTimeParseException e) {
            System.err.println("[ERRO] Formato de data inválido. Use AAAA-MM-DD.");
        } catch (NumberFormatException e) {
            System.err.println("[ERRO] Formato de tempo necessário inválido. Insira um número inteiro.");
        }
    }

    /**
     * Exibe todas as tarefas atualmente armazenadas no {@link Workspace} em
     * formato de tabela simples. Se não existirem tarefas, imprime uma mensagem
     * de notificação.
     */
    private static void listTasks() {
        List<Task> tasks = workspace.getTasks();
        if (tasks.isEmpty()) {
            System.out.println("\n[!] Nenhuma tarefa no sistema.");
            return;
        }

        String header = "+----+----------------------+-------------+------------+----------------+";
        System.out.println("\n" + header);
        System.out.printf("| %-2s | %-20s | %-11s | %-10s | %-14s |%n", "ID", "TÍTULO", "STATUS", "DEADLINE", "TEMPO ESPERADO");
        System.out.println(header);

        for (Task t : tasks) {
            System.out.printf("| %-2d | %-20s | %-11s | %-10s | %-14d |%n",
                t.getId(),
                truncar(t.getTitle(), 20),
                t.getStatus(),
                t.getDeadline(),
                t.getEstimatedEffort());
        }
        System.out.println(header);
        System.out.println("Total de tarefas: " + Task.totalTasksCreated);
    }

    /**
     * Trunca uma string para um comprimento máximo, acrescentando reticências
     * se ela for maior que o tamanho especificado.
     *
     * @param str a string a ser truncada (pode ser {@code null})
     * @param tam o comprimento máximo permitido
     * @return uma string possivelmente reduzida; nunca {@code null}
     */
    private static String truncar(String str, int tam) {
        if (str == null) return "";
        return str.length() > tam ? str.substring(0, tam - 3) + "..." : str;
    }
}
