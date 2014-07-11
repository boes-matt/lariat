package boes.lariat;

public class User {

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";

    public final long id;
    public final String name;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }

}
