package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by phartmann on 23/02/2018.
 */

public final class PetContract {

    private PetContract() {}

    public static class PetEntry implements BaseColumns {

        /* Table Constant */
        public static final String TABLE_NAME = "pets";

        /* Columns Constants */
        //public static final String COLUMN_ID = "_id";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PET_NAME = "name";
        public static final String COLUMN_PET_BREED = "breed";
        public static final String COLUMN_PET_GENDER = "gender";
        public static final String COLUMN_PET_WEIGHT = "weight";

        /* Gender Constants */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
    }
}
