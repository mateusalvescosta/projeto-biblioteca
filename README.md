# Sistema de Gerenciamento de Biblioteca

**Disciplina:** Construção em Estruturas de Dados / Projeto e Qualidade em Engenharia de Software
**Grupo:** Arthur Yuji Mendes Suzuki, Carlos Eduardo Pisa Meireles, Felipe Souza de Jesus e Mateus Alves Costa


## O que é o sistema?

Sistema de gerenciamento de biblioteca com interface via terminal, desenvolvido em Java com JPA + Hibernate e SQLite como banco de dados. O sistema cobre o ciclo completo de uma biblioteca: cadastro de livros, autores, categorias e exemplares; controle de empréstimos com renovação e devolução; fila de reservas com notificação automática ao próximo da fila; geração de multa por atraso; relatórios gerenciais; e um módulo de Undo/Redo para reverter operações sem fluxo de exclusão equivalente. O banco de dados é criado automaticamente na primeira execução via `hbm2ddl.auto=update`, sem necessidade de scripts SQL.


## Stack Tecnológica

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Maven | 3.13.0 |
| SQLite | 3.45.1.0 |

### Dependências

| Dependência | Versão | Função |
|---|---|---|
| `jakarta.persistence-api` | 3.1.0 | API JPA para mapeamento objeto-relacional |
| `hibernate-core` | 6.4.4.Final | Implementação do JPA |
| `hibernate-community-dialects` | 6.4.4.Final | Dialeto SQLite para o Hibernate |
| `sqlite-jdbc` | 3.45.1.0 | Driver JDBC do SQLite |
| `slf4j-simple` | 1.7.36 | Log de saída simplificado |
| `lombok` | 1.18.32 | Redução de boilerplate |


## Estrutura de Pacotes

```
br.unisales
│
├── Enumeration/                   # Enumerações de status e tipos do domínio
│
├── database/
│   └── table/                     # Entidades JPA mapeadas para o banco
│       └── primary_key/           # Classes de chave composta (EmbeddedId)
│
├── manager_factory/               # Fábrica de EntityManager (JPA)
│
├── menu/                          # Camada de apresentação (terminal)
│   └── util/                      # Leitura de entrada e limpeza do console
│
└── service/                       # Regras de negócio e acesso ao banco
    └── util/                      # Utilitários compartilhados entre services
```

A arquitetura segue o padrão **Menu → Service → EntityManager**: os menus são responsáveis apenas por coletar entrada do usuário e exibir resultados; toda a lógica de negócio e acesso ao banco vive nos services.


## Modelo de Dados

| Entidade | Tabela | Descrição |
|---|---|---|
| `Livro` | `livro` | Título do acervo, identificado pelo ISBN |
| `Autor` | `autor` | Autor de um ou mais livros |
| `Categoria` | `categoria` | Classificação temática dos livros |
| `LivroAutor` | `livro_autor` | Associação N:N entre `Livro` e `Autor`, com chave composta |
| `LivroCategoria` | `livro_categoria` | Associação N:N entre `Livro` e `Categoria`, com chave composta |
| `Exemplar` | `exemplar` | Cópia física de um livro, com status próprio |
| `Usuario` | `usuario` | Usuário da biblioteca |
| `Emprestimo` | `emprestimo` | Registro de empréstimo de um exemplar a um usuário |
| `Reserva` | `reserva` | Reserva de um livro, formando fila de espera (FIFO) |
| `Multa` | `multa` | Multa por atraso na devolução — R$ 2,00/dia |
| `Notificacao` | `notificacao` | Aviso gerado ao próximo da fila quando um exemplar é devolvido |

### Enumerações

| Enum | Valores |
|---|---|
| `StatusEmprestimoEnum` | `ATIVO`, `RENOVADO`, `DEVOLVIDO`, `ATRASADO`, `CANCELADO` |
| `StatusExemplarEnum` | `DISPONIVEL`, `EMPRESTADO`, `RESERVADO` |
| `StatusReservaEnum` | `RESERVADO`, `ATENDIDA`, `CANCELADO` |
| `UsuarioTipoEnum` | `ALUNO`, `PROFESSOR`, `SERVIDOR` |


## Regras de Negócio

**Empréstimo**
- Usuário bloqueado não pode realizar empréstimo.
- Exemplar deve estar com status `DISPONIVEL`.
- Se houver fila de reserva ativa para o livro, apenas o primeiro da fila pode receber o exemplar — o sistema informa o nome e e-mail do usuário prioritário caso outro tente realizar o empréstimo.
- Ao registrar o empréstimo, a reserva ativa do usuário (se existir) é automaticamente marcada como `ATENDIDA`.
- Prazo padrão de devolução: 7 dias.

**Renovação**
- Permitida apenas uma vez, somente para empréstimos com status `ATIVO`.
- Estende o prazo em mais 7 dias e altera o status para `RENOVADO`, impedindo nova renovação.

**Multa**
- Calculada somente após a devolução, a R$ 2,00 por dia de atraso.
- O sistema impede registro duplicado de multa para o mesmo empréstimo.

**Reservas**
- Usuário bloqueado não pode reservar.
- Usuário com empréstimo ativo (`ATIVO` ou `RENOVADO`) do mesmo livro não pode reservar.
- Fila ordenada por data de criação, atendida sequencialmente (FIFO).
- Ao atender a próxima reserva, uma `Notificacao` é gerada automaticamente com prazo de 2 dias para retirada.

**Usuários**
- O campo `bloqueado` é definido como `false` via `@PrePersist` no momento do cadastro.


## Configuração do Banco de Dados

A persistência é configurada em `src/main/resources/META-INF/persistence.xml`. As entidades mapeadas são declaradas explicitamente com `<class>`, e as propriedades de conexão apontam para um arquivo `.db` local na pasta `dados/`:

```xml
<persistence-unit name="SQLitePU">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <class>br.unisales.database.table.Autor</class>
    <class>br.unisales.database.table.Categoria</class>
    <class>br.unisales.database.table.Livro</class>
    <class>br.unisales.database.table.LivroAutor</class>
    <class>br.unisales.database.table.LivroCategoria</class>
    <class>br.unisales.database.table.Usuario</class>
    <class>br.unisales.database.table.Multa</class>
    <class>br.unisales.database.table.Notificacao</class>
    <class>br.unisales.database.table.Reserva</class>
    <class>br.unisales.database.table.Emprestimo</class>
    <class>br.unisales.database.table.Exemplar</class>

    <properties>
        <property name="jakarta.persistence.jdbc.driver" value="org.sqlite.JDBC"/>
        <property name="jakarta.persistence.jdbc.url"    value="jdbc:sqlite:./dados/biblioteca_db.db"/>
        <property name="hibernate.dialect"               value="org.hibernate.community.dialect.SQLiteDialect"/>
        <property name="hibernate.hbm2ddl.auto"          value="update"/>
        <property name="hibernate.show_sql"              value="false"/>
        <property name="hibernate.format_sql"            value="false"/>
    </properties>
</persistence-unit>
```

O `hbm2ddl.auto=update` cria ou atualiza o schema automaticamente sem apagar dados existentes. Para reiniciar o banco do zero, basta excluir o arquivo `biblioteca_db.db` dentro da pasta `dados/`.


## Como Executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/seu-repositorio.git
   cd seu-repositorio
   ```

2. Crie a pasta `dados/` na raiz do projeto:
   ```bash
   mkdir dados
   ```

3. Compile e execute:
   ```bash
   mvn clean install
   mvn exec:java -Dexec.mainClass="br.unisales.Main"
   ```

Na primeira execução o Hibernate gera automaticamente todas as tabelas no arquivo `./dados/biblioteca_db.db`.


## Decisões Técnicas

### Critério para escolha das operações desfeitas

O módulo de Undo/Redo cobre apenas operações que não possuem uma operação de exclusão direta equivalente no sistema. Cadastrar categoria, livro ou exemplar já possuem seus respectivos métodos de remoção nos menus — o undo dessas ações seria redundante. As operações cobertas foram:

| Operação | Motivo |
|---|---|
| `desfazerEmprestimo` | Não há cancelamento de empréstimo nos menus |
| `desfazerDevolucao` | Não há como reverter uma devolução registrada |
| `desfazerCadastroUsuario` | A remoção de usuário exige validações; o undo é mais direto |
| `desfazerRenovar` | Não há como desfazer uma extensão de prazo pelos menus |
| `desfazerMulta` | Não há exclusão de multa nos menus convencionais |
| `desfazerNotificacao` | Notificações não possuem fluxo de remoção |

### Por que o Redo não foi implementado

O Redo exigiria uma classe de auditoria persistida no banco para que as ações pudessem ser refeitas entre sessões. Essa classe não fazia parte do enunciado do trabalho. Uma pilha em memória seria viável tecnicamente, mas não sobreviveria ao encerramento da aplicação — o que quebraria a consistência de um sistema inteiramente baseado em persistência com JPA. Para manter a coerência do design, optou-se por não implementá-lo.

### O que ficou de fora e por quê

As operações `desfazerRemoverLivro` e `desfazerRemoverExemplar` não foram implementadas. A solução correta seria soft delete — marcar registros como inativos ao invés de excluí-los fisicamente — o que permitiria restaurá-los pelo undo. No entanto, adotar soft delete exigiria revisitar todas as queries JPQL do sistema para filtrar registros inativos, inviável dentro do prazo de entrega (05/05). Para compensar, o sistema aplica validações preventivas que bloqueiam a remoção de registros com vínculos ativos, garantindo integridade sem erros ou exceções.
