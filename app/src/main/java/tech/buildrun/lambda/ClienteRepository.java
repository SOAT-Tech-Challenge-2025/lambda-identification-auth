package tech.buildrun.lambda;

import java.sql.*;
import java.time.LocalDateTime;

public class ClienteRepository {

    public boolean existeCliente(String document) throws SQLException {
        String sql = "SELECT 1 FROM tb_cliente WHERE nr_documento = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, document);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void criarCliente(String name, String email, String document) throws SQLException {
        String sql = "INSERT INTO tb_cliente (nm_cliente, ds_email, nr_documento, dt_inclusao, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, document);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }
}
