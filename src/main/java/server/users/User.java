package server.users;

/**
 * Created by Limmy on 04.06.2017.
 */
public class User {
    private UserGroup group;
    private String userName;
    private String userPassword;

    public User(String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.group = UserGroup.USER;
    }

    public User(UserGroup group, String userName, String userPassword) {
        this.group = group;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public UserGroup getGroup() {
        return group;
    }

    public String getUserName() {
        return userName;
    }

    public boolean checkPassword(String passToBeChecked) {
        return this.userPassword.equals(passToBeChecked);
    }
}
