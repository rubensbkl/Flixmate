package service;

import java.util.ArrayList;

import dao.UserDAO;
import model.User;

public class UserService {
    private UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Autentica um usuário com base no email e senha fornecidos.
     *
     * @param email    O email do usuário
     * @param password A senha do usuário
     * @return true se a autenticação for bem-sucedida, false caso contrário
     */
    public boolean authenticateUser(String email, String password) {
        return userDAO.auth(email, password);
    }

    /**
     * Retorna um usuário com base no email fornecido.
     *
     * @param email O email do usuário
     * @return O objeto User correspondente ao email, ou null se não encontrado
     */
    public User getUserByEmail(String email) {
        return userDAO.getByEmail(email);
    }

    /**
     * Insere um novo usuário no banco de dados.
     *
     * @param user O objeto User a ser inserido
     * @return true se a inserção foi bem-sucedida, false caso contrário
     */
    public boolean insertUser(User user) {
        if (userDAO.insert(user)) {
            return true;
        }
        return false;
    }

    /**
     * Verifica se um endereço de email já existe no banco de dados.
     *
     * @param email O email a ser verificado
     * @return true se o email existir, false caso contrário
     */
    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }

    /**
     * Obtém um usuário com base no ID fornecido.
     *
     * @param id O ID do usuário
     * @return O objeto User correspondente ao ID, ou null se não encontrado
     */
    public User getUserById(int id) {
        return userDAO.getById(id);
    }

    /**
     * Atualiza as informações de um usuário no banco de dados.
     *
     * @param user O objeto User contendo os dados atualizados
     * @return true se a atualização foi bem-sucedida, false caso contrário
     */
    public ArrayList<User> search(String query, int page, int limit) throws Exception {
        return userDAO.search(query, page, limit);
    }

    /**
     * Conta o número de resultados de pesquisa com base em uma consulta.
     *
     * @param query A consulta de pesquisa
     * @return O número de resultados correspondentes à consulta
     */
    public int countSearchResults(String query) {
        return userDAO.countSearchResults(query);
    }

    /**
     * Obtém todos os usuários do banco de dados com paginação.
     *
     * @param page  O número da página para paginação
     * @param limit O número máximo de resultados por página
     * @return Uma lista de usuários correspondentes à paginação
     */
    public ArrayList<User> getAllUsers(int page, int limit) {
        return userDAO.getAllUsers(page, limit);
    }

    /**
     * Conta o número total de usuários no banco de dados.
     *
     * @return O número total de usuários
     */
    public int getTotalUsersCount() {
        return userDAO.getTotalUsersCount();
    }

}
