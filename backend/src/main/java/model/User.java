package model;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private char gender;

    public User() {
        this.id = -1;
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.password = "";
        this.gender = '*';
    }

    public User(int id, String firstName, String lastName, String email, String password, char gender) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.gender = gender;
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

    @Override
    public String toString() {
        return "User [ID = " + id + ", First Name = " + firstName + ", Last Name = " + lastName + 
               ", Email = " + email + ", Password = " + password + ", Gender = " + gender + "]";
    }
}