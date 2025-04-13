package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe abstrata DAO (Data Access Object) que fornece funcionalidades
 * básicas de conexão com o banco de dados para as classes DAO específicas.
 */
public abstract class DAO {
    protected Connection conexao;

    /**
     * Construtor padrão
     */
    public DAO() {
        conexao = null;
    }

    /**
     * Estabelece conexão com o banco de dados PostgreSQL
     * 
     * @param serverName Nome do servidor ou endereço IP
     * @param mydatabase Nome do banco de dados
     * @param porta Número da porta (normalmente 5432 para PostgreSQL)
     * @param username Nome de usuário
     * @param password Senha
     */
    public void conectar(String serverName, String mydatabase, int porta, String username, String password) {
        String url = null;
        try {
            // Carrega o driver do PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // Cria a URL de conexão
            url = "jdbc:postgresql://" + serverName + ":" + porta + "/" + mydatabase;
            
            // Estabelece a conexão
            conexao = DriverManager.getConnection(url, username, password);
            
            if (conexao != null) {
                System.out.println("✅ Conexão efetuada com o PostgreSQL!");
            } else {
                System.err.println("❌ Erro ao conectar no PostgreSQL!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver do PostgreSQL não encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar no PostgreSQL: " + e.getMessage());
            System.err.println("URL: " + url);
        }
    }

    /**
     * Fecha a conexão com o banco de dados
     * 
     * @return true se a conexão foi fechada com sucesso, false caso contrário
     */
    public boolean close() {
        boolean status = false;
        
        if (conexao == null) {
            return true; // Se a conexão já é nula, consideramos como "fechada"
        }
        
        try {
            conexao.close();
            status = true;
            System.out.println("✅ Conexão com PostgreSQL encerrada.");
        } catch (SQLException e) {
            System.err.println("❌ Erro ao fechar conexão: " + e.getMessage());
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