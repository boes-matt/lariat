package boes.lariat;

import android.database.Cursor;
import android.database.CursorWrapper;

public class UserCursor extends CursorWrapper {

    public UserCursor(Cursor c) {
        super(c);
    }

    public User getUser() {
        long id = getLong(getColumnIndex(User.KEY_ID));
        String name = getString(getColumnIndex(User.KEY_NAME));

        return new User(id, name);
    }

}
