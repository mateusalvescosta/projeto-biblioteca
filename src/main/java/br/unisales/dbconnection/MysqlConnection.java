package br.unisales.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.unisales.dbconfig.MysqlConfig;

public final class MysqlConnection {
    private final Connection connection;

    public MysqlConnection() {
        try {
            this.connection = DriverManager.getConnection(MysqlConfig.getUrl(), MysqlConfig.getUser(), MysqlConfig.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível realizar a conexão com o banco de dados MySQL!", e);
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
            throw new RuntimeException("Não foi possível fechar a conexão com o banco de dados MySQL!", e);
        }
    }
}
