package boes.lariat;

public class Place {

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_AUTHOR = "author";

    public final long id;
    public final String name;
    public final User author;

    public Place(long id, String name, User author) {
        this.id = id;
        this.name = name;
        this.author = author;
    }

}
