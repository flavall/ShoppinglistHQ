package com.example.shoppinglisthq;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public class ShoppingMemoDbHelper extends SQLiteOpenHelper {

     private static final String TAG = ShoppingMemoDbHelper.class.getSimpleName();

    public ShoppingMemoDbHelper(@Nullable Context context) {
        super(context, "Platzhalter für DB", null, 1);
        Log.d(TAG, "DbHelper hat die DB angelegt: " + getDatabaseName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
