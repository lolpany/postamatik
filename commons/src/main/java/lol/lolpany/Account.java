package lol.lolpany;

import java.util.List;

public class Account<T> {
    public String email;
    public String login;
    public String password;
    public List<Location<T>> locations;

    public Account(String email, String login, String password, List<Location<T>> locations) {
        this.email = email;
        this.login = login;
        this.password = password;
        this.locations = locations;
    }
}
