package lol.lolpany.postamatik;

public interface PostActionFactory<T extends PostAction, P extends Location> {

    T create(Account account, P location, String id);
}
