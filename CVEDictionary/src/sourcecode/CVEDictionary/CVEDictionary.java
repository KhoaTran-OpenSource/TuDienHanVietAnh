package sourcecode.CVEDictionary;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

public class CVEDictionary extends Activity implements SearchView.OnQueryTextListener {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerLv;
    private TextView mTextdata;
    private ActionBarHelper mActionBar;
    private ActionBarDrawerToggle mDrawerToggle;

    private DatabaseClass mDictionaryDatabase;
    private TextView mTextView;
    private ListView mListView;
    private SearchView mSearchView;
    private int mType = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //For search layout
        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mDictionaryDatabase = new DatabaseClass(this);
        setupSearchView();

        //For Drawer layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLv = (ListView) findViewById(R.id.drawer_listview);

        mDrawerLayout.setDrawerListener(new DemoDrawerListener());
        mDrawerLayout.setDrawerTitle(GravityCompat.START, getString(R.string.drawer_title));

        String[] values = new String[] { "History", "Restore Data", "About"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        mDrawerLv.setAdapter(adapter);
        mDrawerLv.setOnItemClickListener(new DrawerItemClickListener());

        mActionBar = createActionBarHelper();
        mActionBar.init();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer_am, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getResources().getString(R.string.sb_hint_hz));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * The action bar home/up action should open or close the drawer.
         * mDrawerToggle will take care of this.
         */
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        String[] columns = new String[]{
                BaseColumns._ID,
                DatabaseClass.COL_HANZI,
                DatabaseClass.COL_PINYIN,
                DatabaseClass.COL_ENGMEAN,
                DatabaseClass.COL_VIEMEAN
        };
        Cursor cursor = mDictionaryDatabase.getWordMatches(query, columns, mType);
        if((cursor != null) && (query.length() != 0)) {
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                    count, new Object[]{count, query});
            mTextView.setText(countString);

            // Specify the columns we want to display in the result
            String[] from = new String[]{DatabaseClass.COL_HANZI,
                    DatabaseClass.COL_PINYIN,
                    DatabaseClass.COL_ENGMEAN,
                    DatabaseClass.COL_VIEMEAN};

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[]{R.id.res_word,
                    R.id.res_pinyin,
                    R.id.res_englmean,
                    R.id.res_vietmean};

            CustomeListViewData words = new CustomeListViewData(this, R.layout.result, cursor, from, to, mType, query);
            mListView.setAdapter(words);

            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
                    Cursor cursor_t = (Cursor) mListView.getItemAtPosition(position);
                    wordIntent.putExtra("HANZI", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_HANZI)));
                    wordIntent.putExtra("PINYIN", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_PINYIN)));
                    wordIntent.putExtra("ENGMEAN", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_ENGMEAN)));
                    wordIntent.putExtra("VIETMEAN", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_VIEMEAN)));
                    startActivity(wordIntent);
                }
            });
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String[] columns = new String[]{
                BaseColumns._ID,
                DatabaseClass.COL_HANZI,
                DatabaseClass.COL_PINYIN,
                DatabaseClass.COL_ENGMEAN,
                DatabaseClass.COL_VIEMEAN
        };
        Cursor cursor = mDictionaryDatabase.getWordMatches(newText, columns, mType);
        if((cursor != null) && (newText.length() != 0)) {
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                    count, new Object[]{count, newText});
            mTextView.setText(countString);

            // Specify the columns we want to display in the result
            String[] from = new String[]{DatabaseClass.COL_HANZI,
                    DatabaseClass.COL_PINYIN,
                    DatabaseClass.COL_ENGMEAN,
                    DatabaseClass.COL_VIEMEAN};

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[]{R.id.res_word,
                    R.id.res_pinyin,
                    R.id.res_englmean,
                    R.id.res_vietmean};

            CustomeListViewData words = new CustomeListViewData(this, R.layout.result, cursor, from, to, mType, newText);
            mListView.setAdapter(words);

            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
                    Cursor cursor_t = (Cursor) mListView.getItemAtPosition(position);
                    wordIntent.putExtra("HANZI", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_HANZI)));
                    wordIntent.putExtra("PINYIN", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_PINYIN)));
                    wordIntent.putExtra("VIETMEAN", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_ENGMEAN)));
                    wordIntent.putExtra("ENGMEAN", cursor_t.getString(cursor_t.getColumnIndexOrThrow(DatabaseClass.COL_VIEMEAN)));
                    startActivity(wordIntent);
                }
            });
        }
        return false;
    }

    /**
     * This list item click listener implements very simple view switching by changing
     * the primary content text. The drawer is closed when a selection is made.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //mTextdata.setText("Postion: " + position);
            //mDrawerLayout.closeDrawer(mDrawerLv);
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    public void onChinese(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        mSearchView.setQueryHint(getResources().getString(R.string.sb_hint_hz));
        mType = 0;
    }

    public void onPinYin(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        mSearchView.setQueryHint(getResources().getString(R.string.sb_hint_py));
        mType = 1;
    }

    public void onVietNamese(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        mSearchView.setQueryHint(getResources().getString(R.string.sb_hint_vm));
        mType = 3;
    }

    public void onEnglish(MenuItem item) {
        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        mSearchView.setQueryHint(getResources().getString(R.string.sb_hint_em));
        mType = 2;
    }

    private class DemoDrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {
            mDrawerToggle.onDrawerOpened(drawerView);
            mActionBar.onDrawerOpened();
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            mDrawerToggle.onDrawerClosed(drawerView);
            mActionBar.onDrawerClosed();
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            mDrawerToggle.onDrawerStateChanged(newState);
        }
    }

    /**
     * Create a compatible helper that will manipulate the action bar if available.
     */
    private ActionBarHelper createActionBarHelper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Log.d("DrawerLayout", "ICE_CREAM_SANDWICH");
            return new ActionBarHelperICS();
        } else {
            Log.d("DrawerLayout", "GingerBread");
            return new ActionBarHelper();
        }
    }

    /**
     * Stub action bar helper; this does nothing.
     */
    private class ActionBarHelper {
        public void init() {}
        public void onDrawerClosed() {}
        public void onDrawerOpened() {}
        public void setTitle(CharSequence title) {}
    }

    /**
     * Action bar helper for use on ICS and newer devices.
     */
    private class ActionBarHelperICS extends ActionBarHelper {
        private final ActionBar mActionBar;
        private CharSequence mDrawerTitle;
        private CharSequence mTitle;

        ActionBarHelperICS() {
            mActionBar = getActionBar();
        }

        @Override
        public void init() {
            mActionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
            mTitle = mDrawerTitle = getTitle();
        }

        /**
         * When the drawer is closed we restore the action bar state reflecting
         * the specific contents in view.
         */
        @Override
        public void onDrawerClosed() {
            super.onDrawerClosed();
            mActionBar.setTitle(mTitle);
            mActionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
        }

        /**
         * When the drawer is open we set the action bar to a generic title.
         * The action bar should only contain data relevant at the top level of
         * the nav hierarchy represented by the drawer, as the rest of your content
         * will be dimmed down and non-interactive.
         */
        @Override
        public void onDrawerOpened() {
            super.onDrawerOpened();
            mActionBar.setTitle(mDrawerTitle);
            mActionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
        }

        @Override
        public void setTitle(CharSequence title) {
            mTitle = title;
        }
    }
}
