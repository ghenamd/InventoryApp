package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ghena on 06/07/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Inventory.db";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InventoryContract.InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " (" +
                InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE + " DOUBLE NOT NULL DEFAULT 0.0, " +
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE + " TEXT );";
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
