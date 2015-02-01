package com.rules.perseus.limbo99;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String TABLE_EN = "table_en";
    public static final String TABLE_DE = "table_de";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WORD = "word";

    private static final String DATABASE_NAME = "language_references.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_EN = "create table "
            + TABLE_EN + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_WORD
            + " text not null);";

    private static final String CREATE_TABLE_DE = "create table "
            + TABLE_DE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_WORD
            + " text not null);";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_EN);
        database.execSQL(CREATE_TABLE_DE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHandler.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DE);
        onCreate(db);
    }

}
