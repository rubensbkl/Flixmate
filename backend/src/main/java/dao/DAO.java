package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe abstrata DAO (Data Access Object) que fornece funcionalidades
 * básicas de conexão com o banco de dados para as classes DAO específicas,
 * utilizando HikariCP para pool de conexões.
 */
public abstract class DAO {
    protected Connection conexao;
    private static HikariDataSource dataSource;

    /**
     * Construtor padrão
     */
    public DAO() {
        conexao = null;
    }

    /**
     * Estabelece conexão com o banco de dados PostgreSQL usando HikariCP.
     * Se o pool ainda não estiver inicializado, ele o inicializa.
     * 
     * @param serverName Nome do servidor ou endereço IP
     * @param mydatabase Nome do banco de dados
     * @param porta Número da porta (normalmente 5432 para PostgreSQL)
     * @param username Nome de usuário
     * @param password Senha
     */
    public void conectar(String serverName, String mydatabase, int porta, String username, String password) {
        try {
            if (dataSource == null) {
                synchronized (DAO.class) {
                    if (dataSource == null) {
                        HikariConfig config = new HikariConfig();
                        String url = "jdbc:postgresql://" + serverName + ":" + porta + "/" + mydatabase;
                        config.setJdbcUrl(url);
                        config.setUsername(username);
                        config.setPassword(password);
                        config.setMaximumPoolSize(10); // Ajuste conforme necessidade
                        config.setMinimumIdle(2);
                        config.setConnectionTimeout(30000);
                        config.setIdleTimeout(600000);
                        config.setMaxLifetime(1800000);
                        config.setDriverClassName("org.postgresql.Driver");
                        dataSource = new HikariDataSource(config);
                        System.out.println("✅ HikariCP inicializado com sucesso para PostgreSQL!");
                    }
                }
            }
            conexao = dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar no PostgreSQL via HikariCP: " + e.getMessage());
        }
    }

    /**
     * Fecha (devolve ao pool) a conexão com o banco de dados
     * 
     * @return true se a conexão foi devolvida com sucesso, false caso contrário
     */
    public boolean close() {
        boolean status = false;
        
        if (conexao == null) {
            return true;
        }
        
        try {
            conexao.close();
            status = true;
        } catch (SQLException e) {
            System.err.println("❌ Erro ao devolver conexão ao pool: " + e.getMessage());
        }
        return status;
    }
    
    /**
     * Verifica se a conexão com o banco de dados está ativa
     * 
     * @return true se a conexão está ativa, false caso contrário
     */
    public boolean isConnected() {
        boolean status = false;
        
        try {
            if (conexao != null && !conexao.isClosed()) {
                status = true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao verificar status da conexão: " + e.getMessage());
        }
        
        return status;
    }
}