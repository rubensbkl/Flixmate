package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Genre;

public class GenreDAO extends DAO {

    public GenreDAO(String serverName, String mydatabase, int porta, String username, String password) {
        super();
        conectar(serverName, mydatabase, porta, username, password);
    }

    public void finalize() {
        close();
    }

    /**
     * Insere um novo gênero no banco de dados
     * @param genre O gênero a ser inserido
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
    public boolean insert(Genre genre) {
        boolean status = false;
        try {
            String sql = "INSERT INTO genres (id, name) VALUES (?, ?) ON CONFLICT (id) DO NOTHING";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, genre.getId());
            st.setString(2, genre.getName());
            
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir gênero: " + e.getMessage());
        }
        return status;
    }

    /**
     * Busca um gênero pelo seu ID
     * @param id O ID do gênero a ser buscado
     * @return O gênero encontrado ou null se não encontrado
     */
    public Genre getById(int id) {
        Genre genre = null;
        try {
            String sql = "SELECT * FROM genres WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                genre = new Genre(
                    rs.getInt("id"),
                    rs.getString("name")
                );
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar gênero por ID: " + e.getMessage());
        }
        return genre;
    }

    /**
     * Retorna todos os gêneros cadastrados
     * @return Lista de todos os gêneros
     */
    public List<Genre> getAll() {
        List<Genre> genres = new ArrayList<>();
        try {
            String sql = "SELECT * FROM genres ORDER BY name";
            Statement st = conexao.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Genre genre = new Genre(
                    rs.getInt("id"),
                    rs.getString("name")
                );
                genres.add(genre);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os gêneros: " + e.getMessage());
        }
        return genres;
    }

    /**
     * Atualiza os dados de um gênero
     * @param genre O gênero com os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public boolean update(Genre genre) {
        boolean status = false;
        try {
            String sql = "UPDATE genres SET name = ? WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, genre.getName());
            st.setInt(2, genre.getId());

            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar gênero: " + e.getMessage());
        }
        return status;
    }

    /**
     * Remove um gênero pelo seu ID
     * @param id O ID do gênero a ser removido
     * @return true se a remoção foi bem-sucedida, false caso contrário
     */
    public boolean delete(int id) {
        boolean status = false;
        try {
            String sql = "DELETE FROM genres WHERE id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, id);
            
            int affectedRows = st.executeUpdate();
            status = (affectedRows > 0);
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir gênero: " + e.getMessage());
        }
        return status;
    }

    /**
     * Busca gêneros pelo nome (pesquisa parcial)
     * @param name Parte do nome do gênero
     * @return Lista de gêneros que contêm o texto buscado no nome
     */
    public List<Genre> searchByName(String name) {
        List<Genre> genres = new ArrayList<>();
        try {
            String sql = "SELECT * FROM genres WHERE name ILIKE ? ORDER BY name";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setString(1, "%" + name + "%");
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                Genre genre = new Genre(
                    rs.getInt("id"),
                    rs.getString("name")
                );
                genres.add(genre);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar gêneros por nome: " + e.getMessage());
        }
        return genres;
    }
}