package lol.lolpany.friendify.linkedin;

public interface LinkedInUrlPartGeneratorFactory {

    boolean canCreate(String tag);

    ResettableIterator<String> create(String tag);
}
