package sourcecode.CVEDictionary;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by khoa on 7/9/2015.
 */
public class DatabaseClass {
    private static final String TAG = "DictionaryDatabase";

    //The columns we'll include in the dictionary table
    public static final String COL_HANZI = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String COL_PINYIN = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String COL_ENGMEAN = "englishmeaning";
    public static final String COL_VIEMEAN = "vietmeaning";

    private static final String DATABASE_NAME = "dictionary";
    private static final String FTS_VIRTUAL_TABLE = "FTSdictionary";
    private static final int DATABASE_VERSION = 2;

    private final DictionaryOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String, String> mColumnMap = buildColumnMap();

    /**
     * Constructor
     *
     * @param context The Context within which to work, used to create the DB
     */
    public DatabaseClass(Context context) {
        mDatabaseOpenHelper = new DictionaryOpenHelper(context);
    }

    /**
     * Builds a map for all columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String, String> buildColumnMap() {
        Log.d(TAG, "buildColumnMap");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(COL_HANZI, COL_HANZI);
        map.put(COL_PINYIN, COL_PINYIN);
        map.put(COL_ENGMEAN, COL_ENGMEAN);
        map.put(COL_VIEMEAN, COL_VIEMEAN);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        return map;
    }

    /**
     * Returns a Cursor positioned at the word specified by rowId
     *
     * @param rowId   id of word to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching word, or null if not found.
     */
    public Cursor getWord(String rowId, String[] columns) {
        Log.d(TAG, "getWord");
        String selection = "rowid = ?";
        String[] selectionArgs = new String[]{rowId};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }

    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query   The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getWordMatches(String query, String[] columns) {
        Log.d(TAG, "getWordMatches");
        String selection = COL_HANZI + " LIKE ?";
        //String selection = KEY_DEFINITION + " MATCH ?";
        //String selection = ENG_MEANING + " MATCH ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }

    public Cursor getWordMatches(String query, String[] columns, int type) {
        String selection = COL_HANZI + " LIKE ?";
        if (type == 1) { //Pinyin
            selection = COL_PINYIN + " LIKE ?";
        } else if (type == 2) { //English
            selection = COL_ENGMEAN + " LIKE ?";
        } else if (type == 3) { //Vietnamese
            selection = COL_VIEMEAN + " LIKE ?";
        }

        String[] selectionArgs = new String[]{"%" + query + "%"};

        return query(selection, selectionArgs, columns);
    }

    /**
     * Performs a database query.
     *
     * @param selection     The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns       The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        Log.d(TAG, "query");
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * This creates/opens the database.
     */
    private static class DictionaryOpenHelper extends SQLiteOpenHelper {
        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        /* Note that FTS3 does not support column constraints and thus, you cannot
         * declare a primary key. However, "rowid" is automatically used as a unique
         * identifier, so when making requests, we will use "_id" as an alias for "rowid"
         */
        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_HANZI + ", " +
                        COL_PINYIN + ", " +
                        COL_ENGMEAN + ", " +
                        COL_VIEMEAN + ");";

        DictionaryOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "onCreate");
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDictionary();
        }

        /**
         * Starts a thread to load the database table with words
         */
        private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        private void loadWords() throws IOException {
            Log.d(TAG, "Loading words...");
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.definitions);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "\t");
                    if (strings.length < 2) continue;
                    long id = addWord(strings[0].trim(), strings[1].trim(), strings[2].trim(), strings[3].trim());
                    if (id < 0) {
                        Log.e(TAG, "unable to add word: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
            Log.d(TAG, "DONE loading words.");
        }

        /**
         * Add a word to the dictionary.
         *
         * @return rowId or -1 if failed
         */
        public long addWord(String word, String definition, String engdef, String vietdef) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_HANZI, word);
            initialValues.put(COL_PINYIN, definition);
            initialValues.put(COL_ENGMEAN, engdef);
            if (vietdef.equals("(NULL)")) {
                initialValues.put(COL_VIEMEAN, "\0");
            } else {
                initialValues.put(COL_VIEMEAN, vietdef);
            }

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
    }
}
