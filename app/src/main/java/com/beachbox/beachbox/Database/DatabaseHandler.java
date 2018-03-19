package com.beachbox.beachbox.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.support.customtabs.CustomTabsIntent.KEY_ID;

/**
 * Created by bitwarepc on 04-Jul-17.
 */

/*public class DatabaseHandler {
}*/
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserCart";
    private static final String TABLE_CART_DATA = "Cart";
    private static final String KEY_RESPONSE= "title";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String USER_CART = "CREATE TABLE " + TABLE_CART_DATA + "(" + KEY_RESPONSE + " TEXT" + ")";
        db.execSQL(USER_CART);
    }

    @Override    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART_DATA);
        onCreate(db);
    }

    public void deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_CART_DATA);
    }
// INSERT RECORD

    public void  addResponseSQLite(String strResponse) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_RESPONSE, strResponse);
        db.insert(TABLE_CART_DATA, null, values);
        db.close();
    }


    // GET SINGLE RECORD
    public String getResponseSQLite(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CART_DATA, new String[] {KEY_RESPONSE}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        String mModel=String.valueOf(cursor.getString(0));
        return mModel;
    }

    public int updateResponseSQlite(String strUpdatedResponse) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RESPONSE, strUpdatedResponse);

        // updating row
        return db.update(TABLE_CART_DATA, values, KEY_ID + " = ?",
                new String[] { strUpdatedResponse });
    }

    /*// Deleting single contact
    public void deleteMovie(MovieModel mModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(mModel.getID()) });
        db.close();
    }

// GET COUNT OF RECORD

    public int getMoviesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MOVIES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        System.out.println(">> DB Count in Helper ." + cursor.getCount());
        return cursor.getCount();
    }*/
}