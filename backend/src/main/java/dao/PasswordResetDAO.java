package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PasswordResetDAO extends DAO {

    public PasswordResetDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public boolean createResetToken(int userId, String token, Timestamp expiresAt) {
        String sql = "INSERT INTO password_resets (user_id, token, expires_at) VALUES (?, ?, ?)";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setString(2, token);
            st.setTimestamp(3, expiresAt);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao criar token de reset: " + e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        String sql = "SELECT 1 FROM password_resets WHERE token = ? AND used = false AND expires_at > CURRENT_TIMESTAMP";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, token);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar token: " + e.getMessage());
            return false;
        }
    }

    public int getUserIdByToken(String token) {
        String sql = "SELECT user_id FROM password_resets WHERE token = ? AND used = false AND expires_at > CURRENT_TIMESTAMP";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, token);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar user_id do token: " + e.getMessage());
        }
        return -1;
    }

    public boolean markTokenAsUsed(String token) {
        String sql = "UPDATE password_resets SET used = true WHERE token = ?";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, token);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao marcar token como usado: " + e.getMessage());
            return false;
        }
    }
}
