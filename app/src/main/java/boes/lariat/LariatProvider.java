package boes.lariat;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class LariatProvider extends ContentProvider {

    public static final String AUTHORITY = "com.boes.lariat.provider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri USER_URI  = Uri.withAppendedPath(BASE_URI, "user");
    public static final Uri PLACE_URI = Uri.withAppendedPath(BASE_URI, "place");

    private SQLiteOpenHelper mDBHelper;

    private static final int  USERS = 1;
    private static final int   USER = 2;
    private static final int PLACES = 3;
    private static final int  PLACE = 4;

    private static final UriMatcher URIS = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        URIS.addURI(LariatProvider.AUTHORITY, "user",    USERS);
        URIS.addURI(LariatProvider.AUTHORITY, "user/#",  USER);

        URIS.addURI(LariatProvider.AUTHORITY, "place",   PLACES);
        URIS.addURI(LariatProvider.AUTHORITY, "place/#", PLACE);
    }

    private static final String JOINED_PLACE_TABLE =
            LariatDBHelper.PLACE_TABLE + " JOIN " + LariatDBHelper.USER_TABLE +
                    " ON " + LariatDBHelper.qualifyPlaceColumn(Place.KEY_AUTHOR) +
                    " = "  + LariatDBHelper.qualifyUserColumn(User.KEY_ID);

    private static final String[] QUALIFIED_JOINED_PLACE_COLUMNS = {
            LariatDBHelper.qualifyPlaceColumn(Place.KEY_ID),
            LariatDBHelper.qualifyPlaceColumn(Place.KEY_NAME),
            LariatDBHelper.qualifyPlaceColumn(Place.KEY_AUTHOR),
            LariatDBHelper.qualifyUserColumn(User.KEY_ID),
            LariatDBHelper.qualifyUserColumn(User.KEY_NAME)
    };

    private static final String[] JOINED_PLACE_COLUMNS = {
            Place.KEY_ID,
            Place.KEY_NAME,
            Place.KEY_AUTHOR,
            LariatDBHelper.prefixColumn(Place.KEY_AUTHOR, User.KEY_ID),
            LariatDBHelper.prefixColumn(Place.KEY_AUTHOR, User.KEY_NAME)
    };

    public LariatProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int nDeleted;

        switch (URIS.match(uri)) {
            case PLACE:
                long placeId = ContentUris.parseId(uri);

                String whereClause = Place.KEY_ID + " = ?";
                String[] whereArgs = new String[]{ String.valueOf(placeId) };

                nDeleted = db.delete(LariatDBHelper.PLACE_TABLE, whereClause, whereArgs);
                break;
            case PLACES:
                nDeleted = db.delete(LariatDBHelper.PLACE_TABLE, selection, selectionArgs);
                break;
            case USERS:
                nDeleted = db.delete(LariatDBHelper.USER_TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (nDeleted > 0) getContext().getContentResolver().notifyChange(uri, null);
        return nDeleted;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Implement this to handle requests for the MIME type of the data at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long id;

        switch (URIS.match(uri)) {
            case USERS:
                id = db.insert(LariatDBHelper.USER_TABLE, null, values);
                break;
            case PLACES:
                id = db.insert(LariatDBHelper.PLACE_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri inserted = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(inserted, null);
        return inserted;
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new LariatDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Map<String, String> projectionMap;
        Cursor cursor;

        switch (URIS.match(uri)) {
            case USER:
                long userId = ContentUris.parseId(uri);

                selection = User.KEY_ID + " = ?";
                selectionArgs = new String[]{ String.valueOf(userId) };

                cursor = queryFor(db, LariatDBHelper.USER_TABLE, null, projection, selection, selectionArgs, null);
                break;
            case USERS:
                cursor = queryFor(db, LariatDBHelper.USER_TABLE, null, projection, selection, selectionArgs, null);
                break;
            case PLACE:
                String idKey = LariatDBHelper.qualifyPlaceColumn(Place.KEY_ID);
                long placeId = ContentUris.parseId(uri);

                projectionMap = getProjectionMap(QUALIFIED_JOINED_PLACE_COLUMNS, JOINED_PLACE_COLUMNS);
                selection = idKey + " = ?";
                selectionArgs = new String[]{ String.valueOf(placeId) };

                cursor = queryFor(db, JOINED_PLACE_TABLE, projectionMap, projection, selection, selectionArgs, sortOrder);
                break;
            case PLACES:
                projectionMap = getProjectionMap(QUALIFIED_JOINED_PLACE_COLUMNS, JOINED_PLACE_COLUMNS);

                cursor = queryFor(db, JOINED_PLACE_TABLE, projectionMap, projection, selection, selectionArgs, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor queryFor(SQLiteDatabase db, String table,
                            Map<String, String> projectionMap, String[] projection,
                            String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(table);
        if (projectionMap != null) queryBuilder.setProjectionMap(projectionMap);
        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Map<String, String> getProjectionMap(String[] from, String[] to) {
        Map<String, String> projectionMap = new HashMap<String, String>();
        for (int i = 0; i < from.length; i++) {
            projectionMap.put(from[i], from[i] + " AS " + to[i]);
        }
        return projectionMap;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int nUpdated;

        switch (URIS.match(uri)) {
            case PLACE:
                selection = Place.KEY_ID + " = ?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri)) };
                nUpdated = db.update(LariatDBHelper.PLACE_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (nUpdated > 0) getContext().getContentResolver().notifyChange(uri, null);
        return nUpdated;
    }

}
