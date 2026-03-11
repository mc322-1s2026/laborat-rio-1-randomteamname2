[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/I2-AdLge)
[![Open in Codespaces](https://classroom.github.com/assets/launch-codespace-2972f46106e565e64193e422d61a12cf1da4916b45550586e14ef0a7c637dd04.svg)](https://classroom.github.com/open-in-codespaces?assignment_repo_id=23069476)
# Lab 1: O Núcleo do Nexus — Arquitetura e Consistência

## 1. Sumário da Atividade

Neste laboratório você deverá incrementar um sistema de gestão de tarefas corporativas, o **Nexus**. O foco principal dessa atividade será avaliar a sua capacidade de incrementar um modelo a partir de implementação de métodos e classes representativas, validação de regras de negócio e aplicação de boas práticas de Orientação a Objetos e o uso de Java moderno (Streams, Javadoc, etc). As seções estão divididas em:

1. **O que é o Nexus?** – contexto e requisitos de um motor de gestão corporativa.
2. **Infraestrutura Atual** – análise da estrutura de pacotes pré‑existente e das classes fundamentais (`User`, `Task`, `Workspace`, `LogProcessor`).
3. **Implementação Consistente** – blindagem do modelo a partir do uso de exceções para validação de regras de negócio.
4. **Expansão do Ecossistema** – criação de `Project`, relatórios com Streams e automação do `LogProcessor` para novos comandos.
5. **Avaliação** – critérios de qualidade, métricas e penalidades que nortearão a correção.


## 2. O que é o Nexus?

O **Nexus** não é um ecossistema de gestão corporativa projetado para empresas que operam em escala global. No mercado atual, ferramentas de gestão (como Jira, Trello ou Asana) são o sistema nervoso das organizações. Elas controlam quem faz o quê, quando deve ser entregue e quais são os gargalos de produtividade.

Um motor de gestão como o Nexus precisa lidar com:

* **Alta Concorrência:** Milhares de atualizações de status por segundo.
* **Integridade de Dados:** Uma tarefa concluída não pode voltar a ser "Em Aberto" sem auditoria.
* **Visibilidade Executiva:** Relatórios em tempo real sobre a saúde dos projetos.

## 3. A Infraestrutura Atual

Você recebeu um sistema base que já possui a estrutura fundamental de pacotes e algumas classes de suporte. O sistema está dividido da seguinte forma:

### A. O Modelo de Dados (`com.nexus.model`)

* **`User`**: Representa o agente executor. Atualmente, possui apenas campos básicos de identificação e um esboço para cálculo de carga de trabalho.
* **`Task`**: O coração do sistema. É uma **máquina de estados** que controla o ciclo de vida de uma atividade (`TO_DO` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `DONE`). Ela já possui campos estáticos para telemetria (métricas globais).
* **`TaskStatus`**: Um enumerado que define os estados possíveis de uma tarefa, garantindo que não existam status "inventados".

### B. Os Serviços e Lógica (`com.nexus.service`)

* **`Workspace`**: Atua como o contêiner principal. Ele é responsável por armazenar a lista global de tarefas e oferecer métodos de busca e filtragem.
* **`LogProcessor`**: Uma ferramenta de utilidade que simula o uso real. Ela lê um arquivo de texto (`log_v1.txt`) e converte cada linha em um comando de criação ou alteração no sistema. É aqui que o seu código será testado sob estresse.

### C. Tratamento de Erros (`com.nexus.exception`)

* **`NexusValidationException`**: O sistema adota a filosofia **Fail-Fast**. Em vez de permitir que um dado errado circule, o sistema interrompe a execução e lança esta exceção customizada imediatamente após detectar uma violação de regra de negócio.

## Executando código atual

No VSCode, para abrir o Dev Container dê o comando `Ctrl+Shift+P` -> Dev Containers: Reopen in Container, pode acontecer da construção do container travar após acabar, se acontecer, dê `Ctrl+Shift+P` -> Developer: Reload Window.

No github é possível abrir indo em <> Code -> Codespaces.

Após aberto no container, execute `mvn clean compile exec:java`.

---

## 4. Implementação Consistente: Blindando o Modelo

Nesta etapa, você começará a implementar o sistema de forma que o mesmo seja **auto-validável**. No Nexus, não confiamos que o usuário ou o `LogProcessor` enviará dados corretos. O seu código deve impedir estados inconsistentes através do lançamento de exceções e validações rigorosas (estratégia *Fail-Fast*).

> **Nota do Arquiteto:** "Código que apenas funciona é dívida técnica. Código que protege o estado do sistema é engenharia."

Sempre que uma regra for violada, você deve lançar a **`NexusValidationException`** (para erros de negócio) ou **`IllegalArgumentException`** (para erros de entrada nula/inválida).

### A. Regras de Negócio e Máquina de Estados

#### 1. Classe `User`

O objeto `User` é a fundação da responsabilidade no sistema. Você deve garantir:

* **Integridade de Identidade**: `username` não pode ser nulo, vazio ou composto apenas por espaços.
* **Validação de E-mail**: O e-mail deve seguir o formato padrão `usuario@dominio.com`. Você deve implementar uma verificação (seja via `String.contains()` ou Expressões Regulares) que garanta a presença do caractere `@` e de um endereço válido.
* **Exemplo de Implementação Obrigatória**:
```java
if (username == null || username.isBlank()) {
    throw new IllegalArgumentException("Username não pode ser vazio.");
}
this.username = username;

```


* **Carga de Trabalho**: O método `calculateWorkload()` deve filtrar dinamicamente a lista de tarefas para contar apenas aquelas que estão com status `IN_PROGRESS` e sob a posse deste usuário.

#### 2. Classe `Task`

A `Task` opera como uma máquina de estados finitos. As transições de status não são livres; elas seguem regras de governança:

* **Identidade Imutável**: O `id` gerado para a tarefa no momento da criação **nunca** poderá ser alterado durante todo o ciclo de vida do objeto. Garanta que não existam métodos que permitam essa modificação.
* **Imutabilidade do Prazo**: O `deadline` é definido no nascimento e deve ser protegido contra qualquer alteração posterior.
* **Regra de Transição para `IN_PROGRESS`**: Só é permitido se houver um `User` atribuído como `owner`. Caso contrário, lance `NexusValidationException`.
* **Regra de Transição para `DONE`**: Só é permitido se a tarefa **não** estiver no status `BLOCKED`.
* **Regra de Bloqueio**: Uma tarefa pode ser movida para `BLOCKED` a partir de qualquer estado, exceto se já estiver em `DONE`.
* **Telemetria**: Se uma tentativa de transição falhar por violação de regra, você deve incrementar o contador global `totalValidationErrors` antes de disparar a exceção.

### B. Garantia de Funcionamento

O seu desafio não é apenas escrever o código, mas garantir que ele seja **blindado**. Durante a execução do `LogProcessor`, centenas de comandos tentarão quebrar essas regras propositalmente. Se o seu sistema terminar o processamento com métricas de erro incoerentes ou permitir uma tarefa concluída sem dono ou com e-mail inválido, sua implementação será considerada falha.

**O objetivo é:** O sistema deve ser impossível de ser colocado em um estado inválido.

## 5. Expansão do Ecossistema: Projetos, Inteligência e Automação

O Nexus precisa evoluir para suportar grandes corporações. Nesta fase, você deixará de gerenciar tarefas isoladas e passará a gerenciar **Portfólios de Projetos**, além de automatizar o processamento de grandes volumes de dados.

### A. A Nova Entidade: `Project`

Você deve criar a classe `Project` no pacote `com.nexus.model`. Esta classe será a nova unidade de agrupamento do sistema.

* **Atributos**: Nome, `List<Task>` e um `totalBudget` (em horas).
* **Gestão de Esforço**: Cada `Task` agora deve possuir um campo `estimatedEffort` (horas).
* **Regra de Ouro**: O `Project` deve ter um método `addTask(Task t)`. Este método **deve validar** se a soma das horas de todas as tarefas atuais + a nova tarefa excede o `totalBudget` do projeto. Se exceder, lance `NexusValidationException`.

### B. Inteligência com Java Streams

No `Workspace`, você deve implementar métodos de análise avançada utilizando obrigatoriamente a **Stream API**. O objetivo é gerar relatórios rápidos sem o uso de loops `for` manuais:

1. **Top Performers**: Um método que retorna os 3 usuários que possuem o maior número de tarefas no status `DONE`.
2. **Overloaded Users**: Listar todos os usuários cuja carga de trabalho atual (`IN_PROGRESS`) ultrapassa 10 tarefas.
3. **Project Health**: Para um dado projeto, calcular o percentual de conclusão (Tarefas `DONE` / Total de Tarefas).
4. **Global Bottlenecks**: Identificar qual o status que possui o maior número de tarefas no sistema (exceto `DONE`).

### C. Evolução do `LogProcessor`: Automação Total

O `LogProcessor` original era limitado. Você deve refatorar o método de processamento para que ele consiga interpretar e executar uma gama completa de comandos vindos do arquivo de log. Cada linha do log segue o padrão: `COMANDO;PARAMETRO1;PARAMETRO2...`

Você deve implementar o suporte aos seguintes comandos:

1. **`CREATE_USER;username;email`**: Instancia um novo usuário (valide o e-mail!).
2. **`CREATE_PROJECT;projectName;budgetHours`**: Instancia um novo projeto.
3. **`CREATE_TASK;taskName;deadline;effort;projectName`**: Cria uma tarefa, define seu esforço e a vincula automaticamente ao projeto mencionado.
4. **`ASSIGN_USER;taskId;username`**: Localiza a tarefa pelo ID e o usuário pelo username, realizando a atribuição (Owner).
5. **`CHANGE_STATUS;taskId;newStatus`**: Tenta mover a tarefa para um novo estado (ex: `IN_PROGRESS`, `DONE`, `BLOCKED`). **Atenção**: Este comando deve disparar todas as validações de máquina de estado criadas na Seção 4.
6. **`REPORT_STATUS`**: Aciona a impressão dos relatórios analíticos (Streams) no console.

### D. Desafio de Robustez

Se o arquivo de log contiver um comando para uma tarefa que não existe, ou se um `CHANGE_STATUS` violar uma regra, o `LogProcessor` não deve travar. Ele deve capturar a `NexusValidationException`, incrementar o contador global de erros e prosseguir para a próxima linha do arquivo.

---

## 6. Critérios de Avaliação (Checklist de Qualidade)

O trabalho é em dupla (que deve ser trocada no próximo laboratório). A nota final será composta pela soma dos requisitos técnicos, com penalidades para violações de boas práticas de Orientação a Objetos. Seguem as notas e exemplos de avaliação.

### A. Integridade e Encapsulamento (30%)

* **Proteção de Estado:** As classes `User` e `Task` impedem a criação de objetos inválidos? (Ex: e-mail sem `@`, username vazio, IDs alteráveis após a criação).
* **Encapsulamento:** O uso de `final` e a ausência de métodos `set` em campos sensíveis (como ID e Deadline) foi aplicado corretamente?
* **Modificadores de Acesso:** O uso de `private` para atributos e `public/protected` para métodos foi feito de forma a esconder a implementação interna?

### B. Lógica de Negócio e Máquina de Estados (30%)

* **Transições de Status:** O sistema impede categoricamente mover uma tarefa `BLOCKED` para `DONE` ou `IN_PROGRESS` sem dono?
* **Gestão de Projetos:** A classe `Project` valida corretamente o `totalBudget` ao adicionar novas tarefas?
* **Tratamento de Exceções:** O sistema utiliza `NexusValidationException` para erros de negócio e as exceções são capturadas no `LogProcessor` sem interromper a execução do lote?

### C. Inteligência com Stream API (10%)

* **Uso Declarativo:** Os relatórios de *Top Performers*, *Overloaded Users* e *Project Health* foram implementados usando `Streams`? (O uso de loops `for/while` nestes métodos específicos penalizará a nota).
* **Exatidão Analítica:** Os filtros e ordenações produzem os resultados esperados em tempo razoável mesmo com grandes volumes de dados no log?

### D. Automação e Processamento (15%)

* **Evolução do LogProcessor:** O processador consegue interpretar todos os novos comandos (`CREATE_PROJECT`, `ASSIGN_USER`, etc.)?
* **Telemetria:** Os contadores globais (`totalTasksCreated`, `totalValidationErrors`) refletem a realidade após o processamento?

### E. Qualidade de Código e Padrões (15%)

* **Clean Code:** Nomes de variáveis significativos, métodos pequenos e ausência de código "morto" ou comentado.
* **Trabalho em Dupla:** Evidência de colaboração (histórico de commits equilibrado no repositório) e uso de Github flow.
* **JavaDoc:** Todos os métodos públicos e classes devem possuir documentação JavaDoc

---

## Tabela de Penalidades (Redutores de Nota)

| Violação | Penalidade |
| --- | --- |
| **Bypass de Validação**: Permitir uma tarefa `DONE` que esteja `BLOCKED`. | -2.0 Pontos |
| **Vazamento de Referência**: Retornar a lista original de tarefas em vez de uma cópia imutável. | -1.0 Ponto |
| **Instabilidade**: O programa "quebra" (crash) ao ler uma linha inválida no log. | -2.0 Pontos |
| **Identidade Violada**: Existência de um método `setId()` na classe `Task`. | -1.5 Pontos |