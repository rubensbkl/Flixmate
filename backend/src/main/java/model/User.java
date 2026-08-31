package model;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private char gender;
    private boolean isAdmin;
    private boolean googleConnected;
    private boolean githubConnected;
    private boolean hasPassword;

    public User() {
        this.id = -1;
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.password = "";
        this.gender = '\0';
        this.isAdmin = false;
        this.googleConnected = false;
        this.githubConnected = false;
        this.hasPassword = true;
    }

    public User(int id, String firstName, String lastName, String email, String password, char gender) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.isAdmin = false;
        this.googleConnected = false;
        this.githubConnected = false;
    }

    public User(int id, String firstName, String lastName, String email, String password, char gender, boolean isAdmin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.isAdmin = isAdmin;
        this.googleConnected = false;
        this.githubConnected = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isGoogleConnected() {
        return googleConnected;
    }

    public void setGoogleConnected(boolean googleConnected) {
        this.googleConnected = googleConnected;
    }

    public boolean isGithubConnected() {
        return githubConnected;
    }

    public void setGithubConnected(boolean githubConnected) {
        this.githubConnected = githubConnected;
    }


    public boolean hasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public String toString() {
        return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
                + ", gender=" + gender + ", isAdmin=" + isAdmin + ", googleConnected=" + googleConnected + ", githubConnected=" + githubConnected + "]";
    }
}