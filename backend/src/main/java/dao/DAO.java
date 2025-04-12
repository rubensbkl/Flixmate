package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {
	protected Connection conexao;

	public DAO() {
		conexao = null;
	}

	public void conectar(String serverName, String mydatabase, int porta, String username, String password) {
		boolean status = false;
		String driverName = "org.postgresql.Driver";
		try {
			Class.forName(driverName);
			String url = "jdbc:postgresql://" + serverName + ":" + porta + "/" + mydatabase;
			conexao = DriverManager.getConnection(url, username, password);
			status = (conexao != null);
			if (status) {
				System.out.println("✅ Conexão efetuada com o postgres!");
			} else {
				System.out.println("❌ Erro ao conectar no postgres!");
			}
		} catch (ClassNotFoundException e) {
			System.err.println("❌ Driver do postgres não encontrado: " + e.getMessage());
		} catch (SQLException e) {
			System.err.println("❌ Erro ao conectar no postgres: " + e.getMessage());
		}
	}

	public boolean close() {
		boolean status = false;

		try {
			conexao.close();
			status = true;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		return status;
	}
}