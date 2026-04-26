package br.unisales.menu.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class MenuUtil {

    // Limpa o console imprimindo linhas em branco
    public static void limparConsole() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    // Configura o console para exibir caracteres UTF-8 corretamente
    public static void configurarConsoleUtf8() {
        // Redireciona a saída padrão e de erro para streams com encoding UTF-8
        PrintStream utf8Out = new PrintStream(
                new FileOutputStream(FileDescriptor.out),
                true,
                StandardCharsets.UTF_8
        );
        PrintStream utf8Err = new PrintStream(
                new FileOutputStream(FileDescriptor.err),
                true,
                StandardCharsets.UTF_8
        );
        System.setOut(utf8Out);
        System.setErr(utf8Err);
    }

    // Lê e retorna um número inteiro digitado pelo usuário, repetindo até receber um valor válido
    public static Integer lerInteiro(Scanner scanner, String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                // Informa o erro e solicita nova entrada
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    // Lê e retorna um número Long digitado pelo usuário, repetindo até receber um valor válido
    public static Long lerLong(Scanner scanner, String mensagem) {
        while (true) {
            try {
                System.out.print(mensagem);
                return Long.parseLong(scanner.nextLine());
            } catch (NumberFormatException e) {
                // Informa o erro e solicita nova entrada
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    // Lê e retorna uma linha de texto digitada pelo usuário
    public static String lerTexto(Scanner scanner, String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine();
    }
}