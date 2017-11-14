package lol.lolpany.postamatik;

public interface LocationOutputStreamFactory<T extends Location> {
     LocationOutputStream create(Account account, T location);
}
