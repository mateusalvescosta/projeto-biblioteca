package br.unisales.menu;

import java.util.List;
import java.util.Scanner;

import br.unisales.Enumeration.UsuarioTipoEnum;
import br.unisales.database.table.Usuario;
import br.unisales.manager_factory.ManagerFactory;
import br.unisales.menu.util.MenuUtil;
import br.unisales.service.UsuarioService;

public final class UsuarioMenu {
    private final Scanner scanner;

    // Inicializa o menu de usuários e gerencia o fluxo de interação com o usuário
    public UsuarioMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE CRUD DE USUÁRIO COM JPA     ");
        System.out.println("==========================================");
        ManagerFactory managerFactory = new ManagerFactory("SQLitePU");
        UsuarioService usuarioService = new UsuarioService(managerFactory.get());
        int opcao;
        do {
            exibirMenu();
            opcao = MenuUtil.lerInteiro(this.scanner, "Escolha uma opção: ");

            switch (opcao) {
                case 1 -> cadastrarUsuario(usuarioService);
                case 2 -> listarUsuarios(usuarioService);
                case 3 -> buscarUsuarioPorId(usuarioService);
                case 4 -> bloquearDesbloquearUsuario(usuarioService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        managerFactory.close();
    }

    // Exibe as opções disponíveis no menu de usuários
    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Cadastrar usuário");
        System.out.println("2 - Listar usuários");
        System.out.println("3 - Buscar usuário por ID");
        System.out.println("4 - Bloquear/Desbloquear usuário");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    // Coleta os dados do usuário, define o tipo e aciona o cadastro
    private void cadastrarUsuario(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR USUÁRIO ===");
        String nome = MenuUtil.lerTexto(this.scanner, "Informe o nome: ");
        String email = MenuUtil.lerTexto(this.scanner, "Informe o e-mail: ");
        String senha = MenuUtil.lerTexto(this.scanner, "Informe senha: ");
        String tipo = MenuUtil.lerTexto(this.scanner, "Informe o tipo (ALUNO, PROFESSOR, SERVIDOR): ");

        // Converte o tipo informado para o enum correspondente, usando ALUNO como padrão
        UsuarioTipoEnum stn;
        if (tipo.equalsIgnoreCase("professor"))
            stn = UsuarioTipoEnum.PROFESSOR;
        else if (tipo.equalsIgnoreCase("servidor"))
            stn = UsuarioTipoEnum.SERVIDOR;
        else
            stn = UsuarioTipoEnum.ALUNO;

        Usuario item = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(senha)
                .tipo(stn)
                .build();

        usuarioService.cadastrarUsuario(item);
    }

    // Lista e exibe todos os usuários cadastrados com seus dados e status
    private static void listarUsuarios(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR USUÁRIOS ===");
        List<Usuario> lista = usuarioService.listarUsuarios();
        if (lista.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado.");
            return;
        }
        // Exibe os dados de cada usuário encontrado
        for (Usuario item : lista) {
            System.out.println("-------------------------------------");
            System.out.println("ID: " + item.getId());
            System.out.println("Nome: " + item.getNome());
            System.out.println("E-mail: " + item.getEmail());
            System.out.println("Tipo: " + item.getTipo());
            System.out.println("Status: " + (Boolean.TRUE.equals(item.getBloqueado()) ? "BLOQUEADO" : "ATIVO"));
        }
        System.out.println("-------------------------------------");
    }

    // Busca e exibe os dados de um usuário pelo ID informado
    private void buscarUsuarioPorId(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR USUÁRIO POR ID ===");
        Long id = MenuUtil.lerLong(this.scanner, "Informe o ID do usuário: ");

        // Valida se o usuário existe antes de exibir os dados
        Usuario item = usuarioService.buscarUsuarioPorId(id);
        if (item == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }

        // Exibe os dados completos do usuário encontrado
        System.out.println("-------------------------------------");
        System.out.println("ID: " + item.getId());
        System.out.println("Nome: " + item.getNome());
        System.out.println("E-mail: " + item.getEmail());
        System.out.println("Tipo: " + item.getTipo());
        System.out.println("Status: " + (Boolean.TRUE.equals(item.getBloqueado()) ? "BLOQUEADO" : "ATIVO"));
        System.out.println("-------------------------------------");
    }

    // Coleta o ID do usuário e aciona a alternância do status de bloqueio
    private void bloquearDesbloquearUsuario(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== BLOQUEAR / DESBLOQUEAR USUÁRIO ===");
        Long id = MenuUtil.lerLong(this.scanner, "Informe o ID do usuário: ");
        usuarioService.bloquearDesbloquearUsuario(id);
    }
}