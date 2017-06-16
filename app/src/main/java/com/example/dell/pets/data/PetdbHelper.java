package com.example.dell.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.dell.pets.data.PetsContract.PetsEntry;
import static android.R.attr.name;
import static android.R.attr.version;

/**
 * Created by DELL on 12-02-2017.
 */

public class PetdbHelper extends SQLiteOpenHelper {

   // public static final String LOG_TAG = PetdbHelper.class.getSimpleName();
    private static final String DB_NAME = "shelter.db";
    private static final int DB_VERSION = 1;

    public PetdbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        //string that is used to create the pets table

        String SQL_CREATE_PETS_TABLE = "CREATE TABLE "+ PetsEntry.TABLE_NAME + " ("
                +PetsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                +PetsEntry.COLUMN_PET_NAME + " TEXT NOT NULL,"
                +PetsEntry.COLUMN_PET_BREED+ " TEXT,"
                +PetsEntry.COLUMN_PET_GENDER+ " INTEGER NOT NULL,"
                +PetsEntry.COLUMN_PET_WEIGHT+   "  INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_PETS_TABLE);

    }

    //this method is called when database is updated


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //the database is still at version 1,t
    }
}
