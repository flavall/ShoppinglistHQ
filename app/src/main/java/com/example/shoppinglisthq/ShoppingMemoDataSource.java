package com.example.shoppinglisthq;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ShoppingMemoDataSource {
     private static final String TAG = ShoppingMemoDataSource.class.getSimpleName();

     private SQLiteDatabase database;
     private ShoppingMemoDbHelper dbHelper;

     public ShoppingMemoDataSource (Context context){
         Log.d(TAG, "DataSource erzeugt jetzt den dbHelper");
         dbHelper = new ShoppingMemoDbHelper(context);
     }
}
