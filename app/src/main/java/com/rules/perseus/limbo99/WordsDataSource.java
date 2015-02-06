package com.rules.perseus.limbo99;

        import java.util.ArrayList;
        import java.util.List;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.util.Log;

public class WordsDataSource {

    private static final String TAG = "WordsDataSource";

    // Database fields
    private SQLiteDatabase database;
    private DatabaseHandler dbHelper;
    private String[] allColumns = { DatabaseHandler.COLUMN_ID,
            DatabaseHandler.COLUMN_WORD };

    public WordsDataSource(Context context) {

//        dbHelper = new DatabaseHandler(context);
        dbHelper = DatabaseHandler.getInstance(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {

        dbHelper.close();
    }

    public WordEN createWordEN(String word) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_WORD, word);

        long insertId = database.insert(DatabaseHandler.TABLE_EN, null,
                values);
        Cursor cursor = database.query(DatabaseHandler.TABLE_EN,
                allColumns, DatabaseHandler.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        WordEN newWord = cursorToWordEN(cursor);
        cursor.close();
        return newWord;
    }

    public WordDE createWordDE(String word) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_WORD, word);

        long insertId = database.insert(DatabaseHandler.TABLE_DE, null,
                values);
        Cursor cursor = database.query(DatabaseHandler.TABLE_DE,
                allColumns, DatabaseHandler.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        WordDE newWord = cursorToWordDE(cursor);
        cursor.close();
        return newWord;
    }

    public List<WordEN> getAllWordsEN() {
        List<WordEN> words = new ArrayList<WordEN>();

        Cursor cursor = database.query(DatabaseHandler.TABLE_EN,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            WordEN word = cursorToWordEN(cursor);
            words.add(word);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return words;
    }

    public List<WordDE> getAllWordsDE() {
        List<WordDE> words = new ArrayList<WordDE>();

        Cursor cursor = database.query(DatabaseHandler.TABLE_DE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            WordDE word = cursorToWordDE(cursor);
            words.add(word);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return words;
    }

    public boolean checkIfWordInTable(String language, String queryWord) {
        String tableName = null;
        if (language.equals("de")) {
            tableName = dbHelper.TABLE_DE;
        } else if (language.equals("en")) {
            tableName = dbHelper.TABLE_EN;
        } else {
            Log.e(TAG, "input language: " + language + " not recognized, using default language (EN) instead");
            tableName = dbHelper.TABLE_EN;
        }
        String queryString = "SELECT 1 FROM " + tableName + " WHERE word=\"" + queryWord + "\";";
        //Log.d(TAG, queryString);
        Cursor cursor = database.rawQuery(queryString, null);

        if (cursor.getCount() <= 0) {
            return false;
        }
        return true;
    }

    private WordEN cursorToWordEN(Cursor cursor) {
        WordEN word = new WordEN();
        word.setId(cursor.getLong(0));
        word.setWord(cursor.getString(1));
        return word;
    }

    private WordDE cursorToWordDE(Cursor cursor) {
        WordDE word = new WordDE();
        word.setId(cursor.getLong(0));
        word.setWord(cursor.getString(1));
        return word;
    }
}

