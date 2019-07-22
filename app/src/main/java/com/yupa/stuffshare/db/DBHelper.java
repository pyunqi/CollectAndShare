package com.yupa.stuffshare.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    //Table Name
    public static final String TABLE_NAME = "tbStuff";
    //Column Names
    public static final String COL_STUFF_ID = "_id";
    public static final String COL_STUFF_NAME = "_name";
    public static final String COL_STUFF_PICTURE = "_picture";
    public static final String COL_STUFF_QUANTITY = "_quantity";
    public static final String COL_STUFF_DESCRIPTION = "_description";
    public static final String COL_STUFF_LATITUDE = "_latitude";
    public static final String COL_STUFF_LONGITUDE = "_longitude";
    public static final String COL_STUFF_TAG = "_tag";


    static final String[] columns = new String[]{DBHelper.COL_STUFF_ID,
            DBHelper.COL_STUFF_NAME, DBHelper.COL_STUFF_PICTURE, DBHelper.COL_STUFF_QUANTITY,
            DBHelper.COL_STUFF_DESCRIPTION, DBHelper.COL_STUFF_LATITUDE, DBHelper.COL_STUFF_LONGITUDE, DBHelper.COL_STUFF_TAG};
    //Database Information
    private static final String DATABASE_NAME = "cas.db";
    private static final int DATABASE_VERSION = 1;

    // creation SQLite statement
    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "(" + COL_STUFF_ID + " INTEGER PRIMARY KEY, "
            + COL_STUFF_NAME + " TEXT NOT NULL, " + COL_STUFF_PICTURE + " TEXT NOT NUll," +
            COL_STUFF_QUANTITY + " TEXT NOT NULL, " + COL_STUFF_DESCRIPTION +
            " TEXT NOT NULL, " + COL_STUFF_LATITUDE + " DOUBLE , " + COL_STUFF_LONGITUDE + " DOUBLE , " +
            ""+ COL_STUFF_TAG + " TEXT NOT NULL);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        System.out.println("DB Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        System.out.println("Table Created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        System.out.println("DB Updated");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}