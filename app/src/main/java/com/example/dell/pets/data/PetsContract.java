package com.example.dell.pets.data;

import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;

/**
 * Created by DELL on 12-02-2017.
 */
//outer blankcontract class
public final class PetsContract {
    private PetsContract(){}

        public static final String CONTENT_AUTHORITY = "com.example.dell.pets";
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
        public static final String PATH_PETS = "pets";
    //inner contract class for each table used
    public  static final class PetsEntry implements BaseColumns{
        public final static String TABLE_NAME = "pets";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PET_NAME = "name";//column name used in sqLlite
        public final static String COLUMN_PET_BREED = "breed";
        public final static String COLUMN_PET_GENDER = "gender";
        public final static String COLUMN_PET_WEIGHT = "weight";
        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PETS);

        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        public static boolean isValidGender(Integer gender) {
            switch (gender){
                case 0:return true;
                case 1:return true;
                case 2:return true;
                default:return false;
            }
        }
    }
}
