package lol.lolpany.postamatik;

import java.util.List;

public class Account {
    public String email;
    public String login;
    public String password;
    public List<Location> locations;

    public Account(String email, String login, String password, List<Location> locations) {
        this.email = email;
        this.login = login;
        this.password = password;
        this.locations = locations;
    }
}