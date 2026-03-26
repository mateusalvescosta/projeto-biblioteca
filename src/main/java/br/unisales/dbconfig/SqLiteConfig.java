package br.unisales.dbconfig;

public final class SqLiteConfig {
    private static final String URL = "jdbc:sqlite:biblioteca_db";

    public static String getUrl() {
        return URL;
    }
}
