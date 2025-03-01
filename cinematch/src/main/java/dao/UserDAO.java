package dao;

import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDAO extends DAO {
	
	public UserDAO() {
		super();
		conectar();
	}
	
	public void finalize() {
		close();
	}
	
	public boolean insert(User user) {
	    boolean status = false;
	    
	    try {  
	        Statement st = conexao.createStatement();
	        String sql = "INSERT INTO usuario (id, first_name, last_name, username, password, gender) "
	                   + "VALUES (" + user.getId() + ", '" + user.getFirstName() + "', '" + user.getLastName() + "', '"
	                   + user.getEmail() + "', '" + user.getPassword() + "', '" + user.getGender() + "');";
	        System.out.println(sql);
	        st.executeUpdate(sql);
	        st.close();
	        status = true;
	    } catch (SQLException u) {  
	        throw new RuntimeException(u);
	    }
	    return status;
	}
	
	
	public User getById(int id) {
        User user = null;
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                user = new User(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("gender").charAt(0)
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return user;
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (Statement st = conexao.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("gender").charAt(0)
                );
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return users;
    }

    public boolean update(User user) {
        boolean status = false;
        String sql = "UPDATE usuario SET first_name = ?, last_name = ?, email = ?, password = ?, gender = ? WHERE id = ?";

        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setString(1, user.getFirstName());
            pst.setString(2, user.getLastName());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getPassword());
            pst.setString(5, String.valueOf(user.getGender()));
            pst.setInt(6, user.getId());

            int affectedRows = pst.executeUpdate();
            status = (affectedRows > 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return status;
    }

    public boolean delete(int id) {
		boolean status = false;
		try {  
			Statement st = conexao.createStatement();
			st.executeUpdate("DELETE FROM user WHERE id = " + id);
			st.close();
			status = true;
		} catch (SQLException u) {  
			throw new RuntimeException(u);
		}
		return status;
	}
    
    public boolean auth(String login, String senha) {
		boolean resp = false;
		
		try {
			Statement st = conexao.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			String sql = "SELECT * FROM user WHERE login LIKE '" + login + "' AND senha LIKE '" + senha  + "'";
			System.out.println(sql);
			ResultSet rs = st.executeQuery(sql);
			resp = rs.next();
	        st.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return resp;
	}
}
