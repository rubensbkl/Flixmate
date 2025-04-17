package service;

import dao.UserDAO;
import model.User;

/**
 * Service class responsible for user-related business logic.
 * This class acts as an intermediary between the controllers and the data
 * access objects.
 */
public class UserService {
    private UserDAO userDAO;

    /**
     * Constructs a new UserService with the required dependencies.
     *
     * @param userDAO The data access object for user operations
     */
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user with the provided email and password.
     *
     * @param email    The email of the user to authenticate
     * @param password The password to verify
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticateUser(String email, String password) {
        return userDAO.auth(email, password);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email of the user to retrieve
     * @return The User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        return userDAO.getByEmail(email);
    }

    /**
     * Inserts a new user into the database.
     *
     * @param user The User object to insert
     * @return true if the insertion was successful, false otherwise
     */
    public boolean insertUser(User user) {
        if (userDAO.insert(user)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an email address already exists in the database.
     *
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userDAO.emailExists(email);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve
     * @return The User object if found, null otherwise
     */
    public User getUserById(int id) {
        return userDAO.getById(id);
    }

    /**
     * Gets the content filter setting for a user.
     *
     * @param userId The ID of the user
     * @return true if content filter is enabled, false otherwise
     */
    public boolean getContentFilter(int userId) {
        return userDAO.getContentFilter(userId);
    }

}
