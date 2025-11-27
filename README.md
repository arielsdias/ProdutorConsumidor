# 🌟 **Sistema Produtor & Consumidor (Java + SQLite)**

Um projeto educacional simples, criado para demonstrar:

* como montar um **servidor HTTP em Java**
* como manipular **formulários HTML** sem JavaScript
* como salvar e recuperar dados usando **SQLite embarcado**
* como gerar **páginas HTML dinâmicas** no servidor
* como usar o **IntelliJ IDEA** para rodar aplicações Java

---

# 📚 **1. Objetivo do Projeto**

Este projeto tem duas “funções sociais”:

### 👨‍🏫 Produtor

Cadastra atividades com:

* Nome
* Descrição
* Data

Esses dados são enviados ao servidor e gravados no banco SQLite.

---

### 👀 Consumidor

Visualiza as atividades em **cards separados**, podendo:

* **Curtir** → card fica verde
* **Não curtir** → card fica vermelho

O estado da curtida fica salvo no banco.

Sem JavaScript.
Sem frameworks.
Apenas Java, HTML e um pouco de CSS. 👏

---

# 📦 **2. O que vem no repositório**

```
src/main/java/
│
├── Servidor.java      ← Servidor HTTP + SQLite
├── login.html          ← Tela de login
├── produtor.html       ← Formulário do Produtor
└── estilo.css          ← Estilos dos cartões e das páginas
conteudo.db             ← Banco criado automaticamente
```

---

# 🧰 **3. Tecnologias utilizadas**

| Tecnologia            | Para quê serve                       |
| --------------------- | ------------------------------------ |
| **Java 8+**           | Linguagem principal do servidor      |
| **HttpServer (Java)** | Servidor HTTP embutido               |
| **SQLite**            | Banco de dados local, sem instalação |
| **JDBC SQLite**       | Comunicação Java ↔ Banco             |
| **HTML/CSS**          | Interface simples do aluno           |

---

# 🎯 **4. Como clonar e abrir no IntelliJ (passo a passo detalhado)**

### ▶️ **Passo 1 — Abrir a tela de Clone**

No IntelliJ IDEA:

```
File → New → Project from Version Control
```

### ▶️ **Passo 2 — Colar o link do GitHub**

No campo **URL**, coloque:

```
https://github.com/usuario/repositorio.git
```

Depois clique em **Clone**.

### ▶️ **Passo 3 — Selecionar o Java SDK**

O IntelliJ vai perguntar qual versão do Java usar.

Escolha:

```
Java 17 ou superior (recomendado)
```

Mas o projeto funciona em Java 8+.

### ▶️ **Passo 4 — Abrir o arquivo Servidor.java**

No Project Explorer:

```
src/main/java/Servidor.java
```

Clique nele.

### ▶️ **Passo 5 — Rodar o servidor**

Clique no botão **Run** (setinha verde) perto do método main.

Se tudo estiver certo, aparecerá:

```
Servidor rodando em http://localhost:8080/
```

Pronto! 🎉
Seu servidor está ativo.

---

# 🌐 **5. Como usar a aplicação no navegador**

Abra:

```
http://localhost:8080/
```

Você verá a tela de **Login**.

---

## 👨‍🏫 **Produtor — Cadastrar atividades**

1. Selecione **Produtor**
2. Preencha:

   * Nome
   * Descrição
   * Data
3. Clique **Cadastrar**

A atividade é salva imediatamente no banco `conteudo.db`.

---

## 👀 **Consumidor — Visualizar e curtir atividades**

1. Selecione **Consumidor**
2. Você verá um card para **cada atividade cadastrada**
3. Cada card terá:

   * Nome
   * Descrição
   * Data
   * Status da curtida
   * Botão **Curtir**
   * Botão **Não Curtir**

### 🎨 Comportamento visual

* Ao clicar em **Curtir**, o card fica **verde claro**
* Ao clicar em **Não Curtir**, o card fica **vermelho claro**

Cada ação atualiza diretamente o banco.

---

# 🗃️ **6. Sobre o banco SQLite**

### ✔ Não precisa instalar nada!

O SQLite já vem dentro da biblioteca JDBC.

O arquivo do banco:

```
conteudo.db
```

é criado automaticamente na raiz do projeto.

### ✔ Se apagá-lo, um novo será criado

Assim, você pode reiniciar o projeto do zero a qualquer momento.

---

# 🗃️ **7. Estrutura da Tabela no Banco SQLite**

Este projeto utiliza **apenas uma tabela**, chamada **`dados`**, que armazena tudo o que o Produtor cria e tudo o que o Consumidor avalia.
A tabela é criada automaticamente pelo Java na primeira execução, caso ainda não exista.

---

## 📄 **Tabela: `dados`**

A estrutura da tabela é esta:

| Campo         | Tipo    | Obrigatório       | Descrição                                                                                                     |
| ------------- | ------- | ----------------- | ------------------------------------------------------------------------------------------------------------- |
| **id**        | INTEGER | Sim (PRIMARY KEY) | Identificador único da atividade. É gerado automaticamente.                                                   |
| **nome**      | TEXT    | Sim               | Nome da atividade cadastrada pelo Produtor.                                                                   |
| **descricao** | TEXT    | Sim               | Uma pequena descrição inserida pelo Produtor.                                                                 |
| **data**      | TEXT    | Sim               | Data informada no formulário do Produtor (formato `AAAA-MM-DD`).                                              |
| **curtida**   | TEXT    | Sim               | Pode assumir três valores: `"nenhuma"`, `"curtir"` ou `"nao"`. Representa o status do card para o Consumidor. |

---

## 🏗️ **Como essa tabela é criada pelo servidor**

No início da execução, o servidor utiliza o seguinte comando SQL:

```sql
CREATE TABLE IF NOT EXISTS dados (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT,
    descricao TEXT,
    data TEXT,
    curtida TEXT
)
```

Significa:

* **`id INTEGER PRIMARY KEY AUTOINCREMENT`**
  → Cada atividade cadastrada recebe um número único automaticamente: 1, 2, 3, 4…

* **`nome TEXT`**
  → Guarda o texto digitado no campo "Nome" do Produtor.

* **`descricao TEXT`**
  → Guarda a descrição da atividade.

* **`data TEXT`**
  → Guarda a data escolhida pelo Produtor.

* **`curtida TEXT`**
  → Guarda o status de avaliação do Consumidor.
  Os estados possíveis são:

| Valor       | Significado                     | Cor do card    |
| ----------- | ------------------------------- | -------------- |
| `"nenhuma"` | Ainda não avaliado              | Branco         |
| `"curtir"`  | Consumidor clicou em Curtir     | Verde claro    |
| `"nao"`     | Consumidor clicou em Não Curtir | Vermelho claro |

---

## 🔍 Exemplo de dados reais no banco

Após alguns cadastros e avaliações, a tabela pode ficar assim:

| id | nome    | descricao             | data       | curtida |
| -- | ------- | --------------------- | ---------- | ------- |
| 1  | Aula 1  | Revisão de conteúdo   | 2025-01-10 | curtir  |
| 2  | Projeto | Iniciar protótipo     | 2025-01-12 | nenhuma |
| 3  | Ensaio  | Preparar apresentação | 2025-01-15 | nao     |

---

## 🎓 Resumo didático

A tabela `dados` contém **100% das informações** do sistema:

* O Produtor **adiciona linhas**
* O Consumidor **atualiza a coluna curtida**
* O Servidor **lê tudo e monta os cards dinamicamente**

É uma tabela simples, perfeita para aprender como servidor, HTML e banco podem conversar.

---

# 💡 **8. Entendendo o fluxo da aplicação**

```
[login.html]
     ↓ escolhe tipo
[Produtor] → envia atividade → salva no banco
[Consumidor] → lista atividades → curtir/não curtir → atualiza no banco
```

Simples, direto e fácil de entender 🎯

---

# 🤝 **9. Contribuindo com o projeto**

Sugestões de melhorias possíveis:

* adicionar exclusão de atividades
* adicionar filtro (somente curtidos)
* adicionar paginação
* transformar o projeto em Maven
* trocar HTML estático por templates

Pull Requests são muito bem-vindos! 😄

---

# 📞 **10. Suporte**

* Caso você seja aluno, procure o professor em sala.
* Caso seja desenvolvedor ou deseja aprofundar nos conhecimentos de Java (Front e Back-end), envie um e-mail **professor@arieldias.com.br**.
* Linkedin: https://www.linkedin.com/in/professorarieldias/

---

# 🎉 **10. Obrigado por utilizar este projeto!**

Este é um material criado com foco em **aprendizagem prática**, para que iniciantes entendam como web + banco + Java funcionam juntos.

Se quiser melhorar algo, pedir novos recursos ou adaptar para outra disciplina, é só avisar!
