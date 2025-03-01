package dao;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;


public class DAO {
	protected Connection conexao;

	public DAO() {
		conexao = null;
	}

	public boolean conectar() {
		Dotenv dotenv = Dotenv.load(); // Carrega as variaveis do .env
		
		System.out.println("DB_HOST: " + dotenv.get("DB_HOST"));
		System.out.println("DB_NAME: " + dotenv.get("DB_NAME"));
		System.out.println("DB_PORT: " + dotenv.get("DB_PORT"));
		System.out.println("DB_USER: " + dotenv.get("DB_USER"));
		System.out.println("DB_PASSWORD: " + dotenv.get("DB_PASSWORD"));
		
		String driverName = "org.postgresql.Driver";
        String serverName = dotenv.get("DB_HOST");
        String mydatabase = dotenv.get("DB_NAME");
        int porta = Integer.parseInt(dotenv.get("DB_PORT"));
        String url = "jdbc:postgresql://" + serverName + ":" + porta + "/" + mydatabase;
        String username = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");
        boolean status = false;
		
		try {
			Class.forName(driverName);
			conexao = DriverManager.getConnection(url, username, password);
			status = (conexao == null);
			System.out.println("Conexão efetuada com o postgres!");
		} catch (ClassNotFoundException e) { 
			System.err.println("Conexão NÃO efetuada com o postgres -- Driver não encontrado -- " + e.getMessage());
		} catch (SQLException e) {
			System.err.println("Conexão NÃO efetuada com o postgres -- " + e.getMessage());
		}

		return status;
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