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

    public UsuarioMenu(Scanner scanner) {
        this.scanner = scanner;
        System.out.println("==========================================");
        System.out.println("   SISTEMA DE CRUD DE USUÁRIO COM JPA     ");
        System.out.println("==========================================");
        ManagerFactory emf = new ManagerFactory("SQLitePU");
        UsuarioService usuarioService = new UsuarioService(emf.get());
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> cadastrar(usuarioService);
                case 2 -> listar(usuarioService);
                case 3 -> buscarPorId(usuarioService);
                case 4 -> bloquearDesbloquear(usuarioService);
                case 100 -> System.out.println("Voltando para o menu principal...");
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
            System.out.println();
        } while (opcao != 100);
        emf.close();
    }

    private static void exibirMenu() {
        System.out.println("--------------- MENU ----------------");
        System.out.println("1 - Cadastrar usuário");
        System.out.println("2 - Listar usuários");
        System.out.println("3 - Buscar usuário por ID");
        System.out.println("4 - Bloquear/Desbloquear usuário");
        System.out.println("100 - Voltar");
        System.out.println("-------------------------------------");
    }

    private void cadastrar(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== CADASTRAR USUÁRIO ===");
        String nome = this.lerTexto("Informe o nome: ");
        String email = this.lerTexto("Informe o e-mail: ");
        String senha = this.lerTexto("Informe senha: ");
        String tipo = this.lerTexto("Informe o tipo (ALUNO, PROFESSOR, SERVIDOR): ");

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

    private static void listar(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== LISTAR USUÁRIOS ===");
        List<Usuario> lista = usuarioService.listar();
        if (lista.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado.");
            return;
        }
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

    private void buscarPorId(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== BUSCAR USUÁRIO POR ID ===");
        Long id = this.lerLong("Informe o ID do usuário: ");
        Usuario item = usuarioService.buscarPorId(id);
        if (item == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }
        System.out.println("-------------------------------------");
        System.out.println("ID: " + item.getId());
        System.out.println("Nome: " + item.getNome());
        System.out.println("E-mail: " + item.getEmail());
        System.out.println("Tipo: " + item.getTipo());
        System.out.println("Status: " + (Boolean.TRUE.equals(item.getBloqueado()) ? "BLOQUEADO" : "ATIVO"));
        System.out.println("-------------------------------------");
    }

    private void bloquearDesbloquear(UsuarioService usuarioService) {
        MenuUtil.limparConsole();
        System.out.println("=== BLOQUEAR / DESBLOQUEAR USUÁRIO ===");
        Long id = this.lerLong("Informe o ID do usuário: ");
        usuarioService.bloquearDesbloquear(id);
    }

    private Integer lerInteiro(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Integer.parseInt(this.scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private Long lerLong(String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Long.parseLong(this.scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return this.scanner.nextLine();
    }
}