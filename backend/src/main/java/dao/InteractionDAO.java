package dao;

import java.sql.*;
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
}
