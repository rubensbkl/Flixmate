package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitário para lidar com criptografia de senhas usando BCrypt
 */
public class PasswordUtil {
    
    private static final int ROUNDS = 12;
    
    /**
     * Criptografa uma senha usando BCrypt
     * 
     * @param plainPassword A senha em texto puro
     * @return A senha criptografada (hash)
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(ROUNDS));
    }
    
    /**
     * Verifica se uma senha em texto puro corresponde a um hash
     * 
     * @param plainPassword A senha em texto puro
     * @param hashedPassword O hash da senha armazenado
     * @return true se a senha for válida, false caso contrário
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Erro ao verificar senha: " + e.getMessage());
            return false;
        }
    }
}