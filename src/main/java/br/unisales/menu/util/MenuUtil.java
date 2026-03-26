package br.unisales.menu.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class MenuUtil {
    public static void limparConsole() {
        /*System.out.print("\033[H\033[2J");
        System.out.flush();*/
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }        
    }

    public static void configurarConsoleUtf8() {
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
}
