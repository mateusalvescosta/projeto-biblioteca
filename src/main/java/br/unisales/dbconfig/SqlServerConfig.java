package br.unisales.dbconfig;

public final class SqlServerConfig {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=biblioteca_db;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sqlserver";
    private static final String PASSWORD = "123456";

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
