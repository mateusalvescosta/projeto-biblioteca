package br.unisales.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.unisales.dbconfig.SqlServerConfig;

public class SqlServerConnection {
    private final Connection connection;

    public SqlServerConnection() {
        try {
            this.connection = DriverManager.getConnection(SqlServerConfig.getUrl(), SqlServerConfig.getUser(), SqlServerConfig.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException("Não foi possível realizar a conexão com o banco de dados SQLServer!", e);
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
            throw new RuntimeException("Não foi possível fechar a conexão com o banco de dados SQLServer!", e);
        }
    }
}
