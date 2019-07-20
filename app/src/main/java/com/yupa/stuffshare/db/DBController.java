package com.yupa.stuffshare.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yupa.stuffshare.entity.Stuff;

import java.util.ArrayList;
import java.util.List;

public class DBController {
    // Database fields
    private DBHelper dbHelper;
    private SQLiteDatabase database;

    public DBController(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void close() {
        dbHelper.close();
    }

    public void addStuff(Stuff stuff) {

        database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DBHelper.COL_STUFF_NAME, stuff.get_name());
        values.put(DBHelper.COL_STUFF_PICTURE, stuff.get_picture());
        values.put(DBHelper.COL_STUFF_QUANTITY, stuff.get_quantity());
        values.put(DBHelper.COL_STUFF_TAG, stuff.get_tag());
        values.put(DBHelper.COL_STUFF_LATITUDE, stuff.get_latitude());
        values.put(DBHelper.COL_STUFF_LONGITUDE, stuff.get_longitude());
        values.put(DBHelper.COL_STUFF_DESCRIPTION, stuff.get_description());

        database.insert(DBHelper.TABLE_NAME, null, values);

        System.out.println("Record Added");
        database.close();
    }

    public Stuff getStuff(int _id) {

        database = dbHelper.getReadableDatabase();

        Cursor cursor = database.query(DBHelper.TABLE_NAME, DBHelper.columns, DBHelper.COL_STUFF_ID + " =?",
                new String[]{String.valueOf(_id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }


        Stuff stuff = new Stuff(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getString(2), cursor.getInt(3), cursor.getString(4),
                cursor.getDouble(5), cursor.getDouble(6), cursor.getString(7));

        return stuff;
    }

    // Getting All Stuffs
    public List<Stuff> getAllStuff() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<Stuff> contactList = new ArrayList<Stuff>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBHelper.TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Stuff stuff = new Stuff();
                stuff.set_id(Integer.parseInt(cursor.getString(0)));
                stuff.set_name(cursor.getString(1));
                stuff.set_picture(cursor.getString(2));
                stuff.set_quantity(cursor.getInt(3));
                stuff.set_description(cursor.getString(4));
                stuff.set_latitude(cursor.getDouble(5));
                stuff.set_longitude(cursor.getDouble(6));
                stuff.set_tag(cursor.getString(7));
                // Adding contact to list
                contactList.add(stuff);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Getting All Stuffs
    public List<Stuff> getAllStuffByName(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<Stuff> contactList = new ArrayList<Stuff>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DBHelper.TABLE_NAME + " WHERE "+DBHelper.COL_STUFF_NAME +
                " LIKE '%"+ name +"%'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Stuff stuff = new Stuff();
                stuff.set_id(Integer.parseInt(cursor.getString(0)));
                stuff.set_name(cursor.getString(1));
                stuff.set_picture(cursor.getString(2));
                stuff.set_quantity(cursor.getInt(3));
                stuff.set_description(cursor.getString(4));
                stuff.set_latitude(cursor.getDouble(5));
                stuff.set_longitude(cursor.getDouble(6));
                stuff.set_tag(cursor.getString(7));
                // Adding contact to list
                contactList.add(stuff);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }
    // Updating single stuff
    public int updateStuff(Stuff stuff) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(DBHelper.COL_STUFF_NAME, stuff.get_name());
        values.put(DBHelper.COL_STUFF_QUANTITY, stuff.get_quantity());
        values.put(DBHelper.COL_STUFF_PICTURE, stuff.get_picture());
        values.put(DBHelper.COL_STUFF_DESCRIPTION, stuff.get_description());
        values.put(DBHelper.COL_STUFF_LATITUDE, stuff.get_latitude());
        values.put(DBHelper.COL_STUFF_LONGITUDE, stuff.get_longitude());
        values.put(DBHelper.COL_STUFF_TAG, stuff.get_tag());
        // updating row
        return db.update(DBHelper.TABLE_NAME, values, DBHelper.COL_STUFF_ID + " = ?",
                new String[]{String.valueOf(stuff.get_id())});
    }

    // Deleting single Stuff
    public void deleteEmployee(Stuff stuff) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DBHelper.TABLE_NAME, DBHelper.COL_STUFF_ID + " = ?",
                new String[]{String.valueOf(stuff.get_id())});

        System.out.println("Record Deleted");
        db.close();
    }

    // Deleting single Stuff
    public void deleteStuff(int _id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DBHelper.TABLE_NAME, DBHelper.COL_STUFF_ID + " = ?",
                new String[]{String.valueOf(_id)});
        db.close();
    }
}
