import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import boes.lariat.ApiHelper;
import boes.lariat.LariatProvider;
import boes.lariat.Place;
import boes.lariat.PlaceCursor;
import boes.lariat.User;
import boes.lariat.UserCursor;

public class LariatProviderTest extends ProviderTestCase2<LariatProvider> {

    public LariatProviderTest() {
        super(LariatProvider.class, LariatProvider.AUTHORITY);
    }

    public void testInsertUser() {
        ContentResolver resolver = getMockContentResolver();

        Uri aUserUri = ApiHelper.insertUser(resolver, "Matt");

        Cursor cursor = resolver.query(aUserUri, null, null, null, null);
        assertEquals("Wrong count", 1, cursor.getCount());

        UserCursor userCursor = new UserCursor(cursor);
        userCursor.moveToFirst();
        User user = userCursor.getUser();
        userCursor.close();

        assertEquals("Wrong id", 1, user.id);
        assertEquals("Wrong name", "Matt", user.name);
    }

    public void testInsertPlace() {
        ContentResolver resolver = getMockContentResolver();

        long authorId = ContentUris.parseId(ApiHelper.insertUser(resolver, "Matt"));
        Uri aPlaceUri = ApiHelper.insertPlace(resolver, "Hoover Tower", authorId);

        Cursor cursor = resolver.query(aPlaceUri, null, null, null, null);
        assertEquals("Wrong count", 1, cursor.getCount());

        PlaceCursor placeCursor = new PlaceCursor(cursor);
        placeCursor.moveToFirst();
        Place place = placeCursor.getPlace();
        placeCursor.close();

        assertEquals("Wrong id", 1, place.id);
        assertEquals("Wrong name", "Hoover Tower", place.name);
        assertEquals("Wrong author id", authorId, place.author.id);
        assertEquals("Wrong author name", "Matt", place.author.name);
    }

    public void testQueryForPlaces() {
        ContentResolver resolver = getMockContentResolver();

        long authorId = ContentUris.parseId(ApiHelper.insertUser(resolver, "Matt"));
        ApiHelper.insertPlace(resolver, "Hoover Tower", authorId);
        ApiHelper.insertPlace(resolver, "Rodin Sculptures", authorId);

        Cursor cursor = resolver.query(LariatProvider.PLACE_URI, null, null, null, null);
        assertEquals("Wrong count", 2, cursor.getCount());

        PlaceCursor placeCursor = new PlaceCursor(cursor);
        Set<String> placeNames = new HashSet<String>();
        while (placeCursor.moveToNext()) {
            Place place = placeCursor.getPlace();
            placeNames.add(place.name);
        }
        placeCursor.close();

        List<String> expected = Arrays.asList("Hoover Tower", "Rodin Sculptures");
        assertTrue("Wrong places", placeNames.containsAll(expected));
    }

    public void testQueryForPlacesByAuthor() {
        ContentResolver resolver = getMockContentResolver();

        long authorId = ContentUris.parseId(ApiHelper.insertUser(resolver, "Matt"));
        long rodinId = ContentUris.parseId(ApiHelper.insertPlace(resolver, "Rodin Sculptures", authorId));

        long aDifferentAuthor = 100;
        ApiHelper.insertPlace(resolver, "Hoover Tower", aDifferentAuthor);

        String selection = Place.KEY_AUTHOR + " = ?";
        String[] selectionArgsForMatt = new String[]{ String.valueOf(authorId) };

        Cursor cursorForMatt = resolver.query(LariatProvider.PLACE_URI, null, selection, selectionArgsForMatt, null);
        assertEquals("Wrong count", 1, cursorForMatt.getCount());

        PlaceCursor placeCursorForMatt = new PlaceCursor(cursorForMatt);
        placeCursorForMatt.moveToFirst();
        Place rodin = placeCursorForMatt.getPlace();
        placeCursorForMatt.close();

        assertEquals("Wrong id", rodinId, rodin.id);
        assertEquals("Wrong name", "Rodin Sculptures", rodin.name);
        assertEquals("Wrong author id", authorId, rodin.author.id);
        assertEquals("Wrong author name", "Matt", rodin.author.name);
    }

    public void testDeletePlace() {
        ContentResolver resolver = getMockContentResolver();
        Cursor cursor;

        long authorId = ContentUris.parseId(ApiHelper.insertUser(resolver, "Matt"));
        Uri hooverUri = ApiHelper.insertPlace(resolver, "Hoover Tower", authorId);
        Uri rodinUri = ApiHelper.insertPlace(resolver, "Rodin Sculptures", authorId);

        cursor = resolver.query(LariatProvider.PLACE_URI, null, null, null, null);
        assertEquals("Wrong count", 2, cursor.getCount());
        cursor.close();

        int nDeleted = resolver.delete(hooverUri, null, null);
        assertEquals("Did not delete 1 record", 1, nDeleted);

        cursor = resolver.query(LariatProvider.PLACE_URI, null, null, null, null);
        assertEquals("Wrong count", 1, cursor.getCount());

        PlaceCursor placeCursor = new PlaceCursor(cursor);
        placeCursor.moveToFirst();
        Place place = placeCursor.getPlace();
        placeCursor.close();

        long rodinId = ContentUris.parseId(rodinUri);
        assertEquals("Wrong id", rodinId, place.id);
        assertEquals("Wrong name", "Rodin Sculptures", place.name);
        assertEquals("Wrong author id", authorId, place.author.id);
        assertEquals("Wrong author name", "Matt", place.author.name);
    }

    public void testDeletePlaces() {
        ContentResolver resolver = getMockContentResolver();

        long authorId = ContentUris.parseId(ApiHelper.insertUser(resolver, "Matt"));
        ApiHelper.insertPlace(resolver, "Hoover Tower", authorId);
        ApiHelper.insertPlace(resolver, "Rodin Sculptures", authorId);

        Cursor cursor;

        cursor = resolver.query(LariatProvider.PLACE_URI, null, null, null, null);
        assertEquals("Wrong count", 2, cursor.getCount());
        cursor.close();

        int nDeleted = resolver.delete(LariatProvider.PLACE_URI, null, null);
        assertEquals("Did not delete 2 records", 2, nDeleted);

        cursor = resolver.query(LariatProvider.PLACE_URI, null, null, null, null);
        assertEquals("Wrong count", 0, cursor.getCount());
        cursor.close();
    }

    public void testDeleteUsers() {
        ContentResolver resolver = getMockContentResolver();

        ApiHelper.insertUser(resolver, "Matt");
        ApiHelper.insertUser(resolver, "Jeff");

        Cursor cursor;

        cursor = resolver.query(LariatProvider.USER_URI, null, null, null, null);
        assertEquals("Wrong count", 2, cursor.getCount());
        cursor.close();

        int nDeleted = resolver.delete(LariatProvider.USER_URI, null, null);
        assertEquals("Did not delete 2 records", 2, nDeleted);

        cursor = resolver.query(LariatProvider.USER_URI, null, null, null, null);
        assertEquals("Wrong count", 0, cursor.getCount());
        cursor.close();
    }

    public void testUpdatePlace() {
        ContentResolver resolver = getMockContentResolver();

        long authorId = ContentUris.parseId(ApiHelper.insertUser(resolver, "Matt"));
        Uri aPlaceUri = ApiHelper.insertPlace(resolver, "Hoover", authorId);

        ContentValues values = new ContentValues();
        values.put(Place.KEY_NAME, "Hoover Tower");
        int nUpdated = resolver.update(aPlaceUri, values, null, null);
        assertEquals("Did not update 1 record", 1, nUpdated);

        Cursor cursor = resolver.query(aPlaceUri, null, null, null, null);
        PlaceCursor placeCursor = new PlaceCursor(cursor);
        placeCursor.moveToFirst();
        Place place = placeCursor.getPlace();
        placeCursor.close();

        long placeId = ContentUris.parseId(aPlaceUri);

        assertEquals("Wrong id", placeId, place.id);
        assertEquals("Wrong name", "Hoover Tower", place.name);
        assertEquals("Wrong author id", authorId, place.author.id);
        assertEquals("Wrong author name", "Matt", place.author.name);
    }

}
