package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DAO {
	protected Connection conexao;

	public DAO() {
		conexao = null;
	}

	public boolean conectar() {
		String env = System.getenv("ENV"); // Pode ser dev ou production

		// Carrega o .env correto baseado no ambiente
		Dotenv dotenv = Dotenv.configure()
			.filename(env.equals("production") ? ".env.production" : ".env.dev")
			.load();

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
			status = (conexao != null);
			System.out.println("✅ Conexão efetuada com o postgres!");
		} catch (ClassNotFoundException e) {
			System.err.println("❌ Driver do postgres não encontrado: " + e.getMessage());
		} catch (SQLException e) {
			System.err.println("❌ Erro ao conectar no postgres: " + e.getMessage());
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