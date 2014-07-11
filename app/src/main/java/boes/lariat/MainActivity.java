package boes.lariat;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Random;

public class MainActivity extends Activity implements
        AdapterView.OnItemLongClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private CursorAdapter mAdapter;

    private static final String[] AUTHORS = { "Billy the Kid", "Wyatt Earp", "Pancho Villa", "Davy Crockett" };
    private static final String[] PLACES = { "Yuma", "Tombstone", "Border town", "Dodge City", "The Alamo" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] from = { Place.KEY_NAME, LariatDBHelper.prefixColumn(Place.KEY_AUTHOR, User.KEY_NAME) };
        int[] to = { android.R.id.text1, android.R.id.text2 };
        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, from, to, 0);

        ListView listView = new ListView(this);
        listView.setAdapter(mAdapter);
        listView.setOnItemLongClickListener(this);
        setContentView(listView);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_place) {
            Random r = new Random();
            int a = r.nextInt(AUTHORS.length);
            int p = r.nextInt(PLACES.length);

            // This adds a new author for each new place.
            // So there could be multiple Davy Crockett's even though we all know there is only ONE Davy Crockett.
            // Future iteration might select author from existing set of authors.  Being lazy right now.
            Uri aUserUri = ApiHelper.insertUser(getContentResolver(), AUTHORS[a]);
            ApiHelper.insertPlace(getContentResolver(), PLACES[p], ContentUris.parseId(aUserUri));
            return true;
        }

        if (id == R.id.clear_db) {
            getContentResolver().delete(LariatProvider.PLACE_URI, null, null);
            getContentResolver().delete(LariatProvider.USER_URI, null, null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Uri aPlaceUri = ContentUris.withAppendedId(LariatProvider.PLACE_URI, id);
        getContentResolver().delete(aPlaceUri, null, null);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, LariatProvider.PLACE_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
