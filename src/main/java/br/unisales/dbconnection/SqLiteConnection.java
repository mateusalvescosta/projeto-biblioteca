package br.unisales.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.unisales.dbconfig.SqLiteConfig;

public class SqLiteConnection {
    private final Connection connection;

    public SqLiteConnection() {
        try {
            this.connection = DriverManager.getConnection(SqLiteConfig.getUrl());
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível realizar a conexão com o banco de dados SqLite!", e);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void close() {
        if (this.connection == null) {
            return;
        }
        try {
            if (!this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível fechar a conexão com o banco de dados SqLite!", e);
        }
    }
}
