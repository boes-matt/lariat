package boes.lariat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

public class ApiHelper {

    public static Uri insertUser(ContentResolver resolver, String name) {
        ContentValues userValues = new ContentValues();
        userValues.put(User.KEY_NAME, name);
        return resolver.insert(LariatProvider.USER_URI, userValues);
    }

    public static Uri insertPlace(ContentResolver resolver, String name, long authorId) {
        ContentValues placeValues = new ContentValues();
        placeValues.put(Place.KEY_NAME, name);
        placeValues.put(Place.KEY_AUTHOR, authorId);
        return resolver.insert(LariatProvider.PLACE_URI, placeValues);
    }

}
