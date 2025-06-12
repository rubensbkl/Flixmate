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

    /**
     * Insere um novo usuário no banco de dados
     * 
     * @param user O usuário a ser inserido
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
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

    /**
     * Busca um usuário pelo ID
     * 
     * @param id O ID do usuário a ser buscado
     * @return O usuário encontrado ou null se não encontrado
     */
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
                            rs.getString("gender").charAt(0));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID: " + e.getMessage());
        }
        return user;
    }

    /**
     * Busca todos os usuários no banco de dados
     * 
     * @return Lista de usuários
     */
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
                        rs.getString("gender").charAt(0)));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os usuários: " + e.getMessage());
        }
        return users;
    }

    /**
     * Atualiza os dados de um usuário existente
     * 
     * @param user O usuário com os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean update(User user) {
        boolean status = false;
        // Primeiro, mantém ou re-hash da senha
        User current = getById(user.getId());
        String senhaParaSalvar = user.getPassword().equals(current.getPassword())
                ? user.getPassword()
                : PasswordUtil.hashPassword(user.getPassword());
        String sql = "UPDATE users SET " +
                "first_name = ?, last_name = ?, email = ?, password = ?, gender = ? " +
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

    /**
     * Exclui um usuário pelo ID
     * 
     * @param id O ID do usuário a ser excluído
     * @return true se a exclusão foi bem-sucedida, false caso contrário
     */
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

    /**
     * Autentica um usuário com base no email e senha
     * 
     * @param email    O email do usuário
     * @param password A senha do usuário
     * @return true se a autenticação for bem-sucedida, false caso contrário
     */
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

    /**
     * Verifica se um email já existe no banco de dados
     * 
     * @param email O email a ser verificado
     * @return true se o email já existir, false caso contrário
     */
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

    /**
     * Busca um usuário pelo email
     * 
     * @param email O email do usuário a ser buscado
     * @return O usuário encontrado ou null se não encontrado
     */
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
                            rs.getString("gender").charAt(0));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por email: " + e.getMessage());
        }
        return user;
    }

    /**
     * Busca usuários com base em uma consulta de pesquisa
     * 
     * @param query A consulta de pesquisa (pode ser parte do nome ou email)
     * @param page  Número da página para paginação
     * @param limit Número máximo de resultados por página
     * @return Lista de usuários que correspondem à consulta
     */
    public ArrayList<User> search(String query, int page, int limit) {
        ArrayList<User> users = new ArrayList<>();

        String sql = "SELECT id, first_name, last_name, email FROM users " +
                "WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? " +
                "ORDER BY first_name ASC " +
                "LIMIT ? OFFSET ?";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            String likeQuery = "%" + query.toLowerCase() + "%";
            st.setString(1, likeQuery);
            st.setString(2, likeQuery);
            st.setString(3, likeQuery);
            st.setInt(4, limit);
            st.setInt(5, (page - 1) * limit);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuários: " + e.getMessage(), e);
        }

        return users;
    }

    /**
     * Conta o número total de usuários que correspondem a uma consulta de pesquisa
     * 
     * @param query A consulta de pesquisa (pode ser parte do nome ou email)
     * @return Número total de usuários que correspondem à consulta
     */
    public int countSearchResults(String query) {
        int total = 0;

        String sql = "SELECT COUNT(*) AS total FROM users " +
                "WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ?";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            String likeQuery = "%" + query.toLowerCase() + "%";
            st.setString(1, likeQuery);
            st.setString(2, likeQuery);
            st.setString(3, likeQuery);

            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar usuários: " + e.getMessage(), e);
        }

        return total;
    }

    /**
     * Busca todos os usuários com paginação
     * 
     * @param page  Número da página para paginação
     * @param limit Número máximo de resultados por página
     * @return Lista de usuários na página especificada
     */
    public ArrayList<User> getAllUsers(int page, int limit) {
        ArrayList<User> users = new ArrayList<>();

        String sql = "SELECT id, first_name, last_name, email FROM users " +
                "ORDER BY first_name ASC " +
                "LIMIT ? OFFSET ?";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, limit);
            st.setInt(2, (page - 1) * limit);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuários: " + e.getMessage(), e);
        }

        return users;
    }

    /**
     * Conta o total de usuarios no banco de dados
     * 
     * @return Número total de usuarios
     */
    public int getTotalUsersCount() {
        int total = 0;

        String sql = "SELECT COUNT(*) AS total FROM users";

        try {
            PreparedStatement st = conexao.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar usuários: " + e.getMessage(), e);
        }

        return total;
    }

}