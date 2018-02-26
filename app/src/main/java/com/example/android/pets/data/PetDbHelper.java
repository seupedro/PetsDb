package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by phartmann on 23/02/2018.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    /* DB Constants */
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "pets.db";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PetEntry.TABLE_NAME + " (" +
                    PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, " +
                    PetEntry.COLUMN_PET_BREED + " TEXT NOT NULL, " +
                    PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL DEFAULT 0, " +
                    PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0" + ")";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;

    /* Constructor */
    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* Call SQL parameters to create */
    @Override
    public void onCreate( SQLiteDatabase sqLiteDatabase ) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    /**/
    @Override
    public void onUpgrade( SQLiteDatabase sqLiteDatabase, int olderVersion, int newerVersion ) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
