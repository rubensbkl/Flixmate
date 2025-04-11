package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Interaction;

public class InteractionDAO extends DAO {

    public InteractionDAO() {
        super();
        conectar();
    }

    public void finalize() {
        close();
    }

    public boolean insert(Interaction interaction) {
        boolean status = false;

        try {
            String sql = "INSERT INTO interactions (user_id, movie_id, interaction) VALUES (?, ?, ?)";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, interaction.getUserId());
            st.setInt(2, interaction.getMovieId());
            st.setBoolean(3, interaction.getInteraction());
            st.executeUpdate();
            st.close();
            status = true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return status;
    }

    public List<Interaction> getInteractionsByUserId(int userId) {
        List<Interaction> interacoes = new ArrayList<>();

        try {
            String sql = "SELECT * FROM interactions WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                int movieId = rs.getInt("movie_id");
                boolean interaction = rs.getBoolean("interaction");

                interacoes.add(new Interaction(userId, movieId, interaction));
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("Erro ao buscar interações: " + e.getMessage());
        }

        return interacoes;
    }


    public boolean clear(int userId) {
        boolean status = false;

        try {
            String sql = "DELETE FROM interactions WHERE user_id = ?";
            PreparedStatement st = conexao.prepareStatement(sql);
            st.setInt(1, userId);
            st.executeUpdate();
            st.close();
            status = true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return status;
    }
}
