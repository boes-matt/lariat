package boes.lariat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LariatDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "lariat.db";
    private static final int VERSION = 1;

    public static final String  USER_TABLE = "user";
    public static final String PLACE_TABLE = "place";

    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + LariatDBHelper.USER_TABLE + " (" +
            User.KEY_ID + " INTEGER PRIMARY KEY, " +
            User.KEY_NAME + " TEXT" +
            ")";

    private static final String CREATE_PLACE_TABLE =
            "CREATE TABLE " + LariatDBHelper.PLACE_TABLE + " (" +
            Place.KEY_ID + " INTEGER PRIMARY KEY, " +
            Place.KEY_NAME + " TEXT, " +
            Place.KEY_AUTHOR + " INTEGER NOT NULL" +
            ")";

    private static final String DROP_USER_TABLE =
            "DROP TABLE IF EXISTS " + LariatDBHelper.USER_TABLE;

    private static final String DROP_PLACE_TABLE =
            "DROP TABLE IF EXISTS " + LariatDBHelper.PLACE_TABLE;

    public LariatDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_PLACE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_USER_TABLE);
        db.execSQL(DROP_PLACE_TABLE);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static String qualifyUserColumn(String column) {
        return LariatDBHelper.USER_TABLE + "." + column;
    }

    public static String qualifyPlaceColumn(String column) {
        return LariatDBHelper.PLACE_TABLE + "." + column;
    }

    public static String prefixColumn(String prefix, String column) {
        return prefix + "_" + column;
    }

}
