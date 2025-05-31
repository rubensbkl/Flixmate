// src/main/java/dao/UserDAO.java
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.User;
import util.PasswordUtil;

public class UserDAO extends DAO {

    public UserDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    @Override
    public void finalize() {
        close();
    }

    public boolean insert(User user) {
        boolean status = false;
        String sql = "INSERT INTO users " +
                     "(first_name, last_name, email, password, gender) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id;";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getEmail());
            st.setString(4, PasswordUtil.hashPassword(user.getPassword()));
            st.setString(5, String.valueOf(user.getGender()));

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                    status = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuário: " + e.getMessage());
        }
        return status;
    }

    public User getById(int id) {
        User user = null;
        String sql = "SELECT first_name, last_name, email, password, gender FROM users WHERE id = ?";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                        id,
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("gender").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
        }
        return user;
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, password, gender FROM users";
        try (Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("gender").charAt(0)
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os usuários: " + e.getMessage());
        }
        return users;
    }

    public boolean update(User user) {
        boolean status = false;
        // Primeiro, mantém ou re-hash da senha
        User current = getById(user.getId());
        String senhaParaSalvar = user.getPassword().equals(current.getPassword())
            ? user.getPassword()
            : PasswordUtil.hashPassword(user.getPassword());
        String sql = "UPDATE users SET " +
                     "first_name = ?, last_name = ?, email = ?, password = ?, gender = ?" +
                     "WHERE id = ?";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getEmail());
            st.setString(4, senhaParaSalvar);
            st.setString(5, String.valueOf(user.getGender()));
            st.setInt(6, user.getId());

            status = st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
        }
        return status;
    }

    // Remove um usuário
    public boolean delete(int id) {
        boolean status = false;
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setInt(1, id);
            status = st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
        }
        return status;
    }

    // Autentica pelo email/senha
    public boolean auth(String email, String password) {
        boolean ok = false;
        String sql = "SELECT password FROM users WHERE email = ?";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    ok = PasswordUtil.checkPassword(password, rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro durante autenticação: " + e.getMessage());
        }
        return ok;
    }

    // Verifica existência de email
    public boolean emailExists(String email) {
        boolean exists = false;
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                exists = rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar email: " + e.getMessage());
        }
        return exists;
    }

    // Busca por email
    public User getByEmail(String email) {
        User user = null;
        String sql = "SELECT id, first_name, last_name, password, gender FROM users WHERE email = ?";
        try (PreparedStatement st = conexao.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        email,
                        rs.getString("password"),
                        rs.getString("gender").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por email: " + e.getMessage());
        }
        return user;
    }

}