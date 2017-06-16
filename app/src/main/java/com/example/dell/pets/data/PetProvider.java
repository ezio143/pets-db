package com.example.dell.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.example.dell.pets.data.PetsContract.PetsEntry;
import static android.R.attr.id;
import static android.R.attr.name;

/**
 * Created by DELL on 13-02-2017.
 */

public class PetProvider extends ContentProvider {


    private SQLiteDatabase db;
    private static final int PETS = 100;
    private static final int PETS_ID = 101;
    //static urimatcher which is used to match the uri's  from the content resolver's
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    //tag for the log messages
    public static final String LOG_TAG = PetdbHelper.class.getSimpleName();
    private PetdbHelper mDbHelper;//pets database helper object

    @Override
    public boolean onCreate() {
        //create and initialize a PetDbhelper object to gain access the access to shelter.db(pets database)
        mDbHelper = new PetdbHelper(getContext());
        return  true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionargs, String sortOrder) {
        //get the database connection with the petdbhelper class
         db = mDbHelper.getReadableDatabase();
        //create a cursor object to return the query result back
        Cursor cursor = null;
        //take a match variable to hold the corresponding uri code
        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS: //do on the entire table
                cursor =  db.query(PetsEntry.TABLE_NAME,projection,selection,selectionargs,null,null,sortOrder);
                break;
            case PETS_ID:
                selection = PetsEntry._ID + "=?";
                selectionargs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetsEntry.TABLE_NAME,projection,selection,selectionargs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("cannot query unknown URI"+uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri,contentValues);
            default:
                throw new IllegalArgumentException("insertion cannot be performed for "+uri);
        }

    }

    //method to insert the pets
    private Uri insertPet(Uri uri,ContentValues values){

        String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);
        if(name == null){
            throw new IllegalArgumentException("pet requires name");
        }

        Integer gender = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
        if(gender == null || !PetsEntry.isValidGender(gender)){
            throw new IllegalArgumentException("pet requires valid gender");
        }

        Integer weight = values.getAsInteger(PetsEntry.COLUMN_PET_WEIGHT);
        if(weight == null && weight < 0){
            throw new IllegalArgumentException("pet requires a weight");
        }

        db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(PetsEntry.TABLE_NAME, null, values);
        if(newRowId == -1){
            Log.e(LOG_TAG,"Failed to insert row for "+uri);
            return null;
        }

        //notify all listeners that the data has changed for the pet content uri
        getContext().getContentResolver().notifyChange(uri,null);

        //return the new uri with the id appeded at the end
        return ContentUris.withAppendedId(uri,newRowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionargs) {

        db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                //notify all listeners that the data has changed for the pet content uri
                getContext().getContentResolver().notifyChange(uri,null);
                return db.delete(PetsEntry.TABLE_NAME,selection,selectionargs);
            case PETS_ID:
                selection = PetsEntry._ID + " =?";
                selectionargs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                //notify all listeners that the data has changed for the pet content uri
                getContext().getContentResolver().notifyChange(uri,null);
                return  db.delete(PetsEntry.TABLE_NAME,selection,selectionargs);
            default:
                throw new IllegalArgumentException("deletion failed"+uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionargs) {

        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return updatePet(uri,contentValues,selection,selectionargs);
            case PETS_ID:
                //for the pet_id code, extract out the id from the uri
                selection = PetsEntry._ID + "=?";
                selectionargs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri,contentValues,selection,selectionargs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+uri);

        }

    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionargs) {
    //update the pets table using the selection args and return the no. of rows updated

        //check the contents of the values passed to update
        if(values.containsKey(PetsEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);
            if(name == null)
                throw new IllegalArgumentException("Pet requires a name");
        }
        if(values.containsKey(PetsEntry.COLUMN_PET_GENDER)){
            Integer gender = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
            if(gender == null || !PetsEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet requires a gender");
            }
        }

        if(values.containsKey(PetsEntry.COLUMN_PET_WEIGHT)){
            Integer weight = values.getAsInteger(PetsEntry.COLUMN_PET_WEIGHT);
            if(weight != null && weight < 0)
                throw new IllegalArgumentException("Pet needs a valid weight");
        }

        //no need to check for the breed of the pet

        //now if no values to update then
        if(values.size() == 0){
            return 0;
        }
        db = mDbHelper.getWritableDatabase();

        //notify all listeners that the data has changed for the pet content uri
        getContext().getContentResolver().notifyChange(uri,null);
        return db.update(PetsEntry.TABLE_NAME,values,selection,selectionargs);

    }


    static {
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,"pets",PETS);//matches the entire pets table
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,"pets/#",PETS_ID);//matches the row specified in the #.

    }
}
