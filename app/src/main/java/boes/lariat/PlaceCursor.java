package boes.lariat;

import android.database.Cursor;
import android.database.CursorWrapper;

public class PlaceCursor extends CursorWrapper {

    public PlaceCursor(Cursor c) {
        super(c);
    }

    public Place getPlace() {
        long id = getLong(getColumnIndex(Place.KEY_ID));
        String name = getString(getColumnIndex(Place.KEY_NAME));

        long authorId = getLong(getColumnIndex(Place.KEY_AUTHOR));
        String columnName = LariatDBHelper.prefixColumn(Place.KEY_AUTHOR, User.KEY_NAME);
        String authorName = getString(getColumnIndex(columnName));

        User author = new User(authorId, authorName);
        return new Place(id, name, author);
    }

}
