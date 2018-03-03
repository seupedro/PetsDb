package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by phartmann on 23/02/2018.
 */

public final class PetContract {

    private PetContract() {}

    /* Autority Constant */
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";

    /* Base URI */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /* Path Constant */
    public static final String PATH_PETS = "pets";
    public static final String PATH_PETS_ID = "pets/#";


    public static class PetEntry implements BaseColumns {

        /**
         * O tipo MIME do {@link #CONTENT_URI} para uma lista de pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /**
         * O tipo MIME do {@link #CONTENT_URI} para um único pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /* Table Constant */
        public static final String TABLE_NAME = "pets";

        /* Columns Constants */
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        /* Gender Constants */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /* Content Uri */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);
    }
}
