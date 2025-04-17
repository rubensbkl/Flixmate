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

    public void finalize() {
        close();
    }

    /**
     * Insere um novo usuário no banco de dados
     * 
     * @param user O usuário a ser inserido
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
    public boolean insert(User user) {
        boolean status = false;
        try {
            String sql = "INSERT INTO users (first_name, last_name, email, password, gender) VALUES (?, ?, ?, ?, ?) RETURNING id;";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getEmail());
            
            // Criptografar a senha antes de salvar
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            st.setString(4, hashedPassword);
            
            st.setString(5, String.valueOf(user.getGender()));

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt(1)); // Define o ID gerado no objeto user
                status = true;
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir usuário: " + e.getMessage());
        }
        return status;
    }

    /**
     * Busca um usuário pelo seu ID
     * 
     * @param id O ID do usuário a ser buscado
     * @return O usuário encontrado ou null se não encontrado
     */
    public User getById(int id) {
        User user = null;
        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("gender").charAt(0));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
        }
        return user;
    }

    /**
     * Retorna todos os usuários cadastrados
     * 
     * @return Lista de todos os usuários
     */
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        try {
            String sql = "SELECT * FROM users";
            Statement st = conexao.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("gender").charAt(0));
                users.add(user);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os usuários: " + e.getMessage());
        }
        return users;
    }

    /**
     * Atualiza os dados de um usuário
     * 
     * @param user O usuário com os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean update(User user) {
        boolean status = false;
        try {
            // Buscar o usuário atual para verificar se a senha foi alterada
            User currentUser = getById(user.getId());
            String passwordToSave;
            
            // Se a senha no objeto é diferente da senha no banco, criptografar a nova senha
            if (!user.getPassword().equals(currentUser.getPassword())) {
                passwordToSave = PasswordUtil.hashPassword(user.getPassword());
            } else {
                // Se a senha não mudou, manter a mesma
                passwordToSave = user.getPassword();
            }
            
            String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, password = ?, gender = ? WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getEmail());
            st.setString(4, passwordToSave);
            st.setString(5, String.valueOf(user.getGender()));
            st.setInt(6, user.getId());

            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
        }
        return status;
    }

    /**
     * Remove um usuário pelo seu ID
     * 
     * @param id O ID do usuário a ser removido
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean delete(int id) {
        boolean status = false;
        try {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, id);
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
        }
        return status;
    }

    /**
     * Autentica um usuário pelo email e senha
     * 
     * @param email    O email do usuário
     * @param password A senha do usuário
     * @return true se autenticação bem-sucedida, false caso contrário
     */
    public boolean auth(String email, String password) {
        boolean resp = false;
        try {
            String sql = "SELECT password FROM users WHERE email = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, email);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                // Verificar a senha usando BCrypt
                resp = PasswordUtil.checkPassword(password, hashedPassword);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro durante autenticação: " + e.getMessage());
        }
        return resp;
    }

    /**
     * Verifica se um email já está em uso
     * 
     * @param email O email a ser verificado
     * @return true se o email já existe, false caso contrário
     */
    public boolean emailExists(String email) {
        boolean exists = false;
        try {
            String sql = "SELECT 1 FROM users WHERE email = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, email);

            ResultSet rs = st.executeQuery();
            exists = rs.next();

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar email: " + e.getMessage());
        }
        return exists;
    }

    /**
     * Busca um usuário pelo seu email
     * 
     * @param email O email do usuário a ser buscado
     * @return O usuário encontrado ou null se não encontrado
     */
    public User getByEmail(String email) {
        User user = null;
        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, email);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("gender").charAt(0));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por email: " + e.getMessage());
        }
        return user;
    }

    /**
     * Obtém a configuração de filtro de conteúdo de um usuário
     * 
     * @param userId O ID do usuário
     * @return true se o filtro de conteúdo estiver ativado, false caso contrário
     */
    public boolean getContentFilter(int userId) {
        boolean contentFilter = false;
        try {
            String sql = "SELECT content_filter FROM users WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
    
            if (rs.next()) {
                contentFilter = rs.getBoolean("content_filter");
            } else {
                System.err.println("Usuário com ID " + userId + " não encontrado.");
            }
    
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar content filter: " + e.getMessage());
        }
    
        return contentFilter;
    }
    
    /**
     * Atualiza a senha de um usuário
     * 
     * @param userId O ID do usuário
     * @param newPassword A nova senha (em texto puro)
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean updatePassword(int userId, String newPassword) {
        boolean status = false;
        try {
            String sql = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            
            // Criptografar a nova senha
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            st.setString(1, hashedPassword);
            st.setInt(2, userId);
            
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar senha: " + e.getMessage());
        }
        return status;
    }
}