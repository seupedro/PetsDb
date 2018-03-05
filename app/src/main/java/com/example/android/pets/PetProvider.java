package com.example.android.pets;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.data.PetDbHelper;

import static com.example.android.pets.data.PetContract.*;

/**
 * Created by phartmann on 01/03/2018.
 */

public class PetProvider extends ContentProvider {

    /* Inicializ Helper and make it global */
    private PetDbHelper helper;

    /* Constants Codes for UriMatcher */
    private static final int PETS = 100;
    private static final int PETS_ID = 101;

    /* Inicialize UriMatcher */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PETS, PETS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_PETS_ID, PETS_ID);
    }

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PetProvider.class.getSimpleName( );

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        helper = new PetDbHelper(getContext( ));
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs,
                         String sortOrder ) {
        // Get readable database
        SQLiteDatabase database = helper.getReadableDatabase( );

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(
                        PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PETS_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert( Uri uri, ContentValues contentValues ) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet( Uri uri, ContentValues values ) {

        // Insert a new pet into the pets database table with the given ContentValues
        // Get SQLdb
        SQLiteDatabase db = helper.getReadableDatabase( );

        //Check if data is valid before insert
        if (checkContentValues(values) == false) {
            Log.e(LOG_TAG, "Invalid inputs were submitted");
            return null;
        } else {
            // Insert data on db
            long id = db.insert(PetEntry.TABLE_NAME, null, values);

            // Log if insertion failed
            if (id == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }
    }

    private boolean checkContentValues( ContentValues values ) {
        if (values.getAsString(PetEntry.COLUMN_PET_NAME) == null ||
                values.getAsString(PetEntry.COLUMN_PET_NAME).isEmpty( )) {
            Toast.makeText(getContext( ), "Name cannot be empty", Toast.LENGTH_SHORT).show( );
            return false;
        }
        if (values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT) <= 0) {
            Toast.makeText(getContext( ), "Weight must be bigger then 0", Toast.LENGTH_SHORT).show( );
            return false;
        }
        if (values.getAsInteger(PetEntry.COLUMN_PET_GENDER) == null) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        return true;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update( Uri uri, ContentValues contentValues, String selection,
                       String[] selectionArgs ) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PETS_ID:
                // Para o código PET_ID, extraia o ID do URI,
                // para que saibamos qual registro atualizar. Selection será "_id=?" and selection
                // args será um String array contendo o atual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Atualize pets no banco de dados com os content values dados. Aplique as mudanças aos registros
     * especificados no selection e selection args (que podem ser 0 ou 1 ou mais pets).
     * Retorne o número de registros que foram atualizados com sucesso.
     */
    private int updatePet( Uri uri, ContentValues values, String selection, String[] selectionArgs ) {

        SQLiteDatabase db = helper.getReadableDatabase( );
        // Atualiza os pets selecionados na tabela de banco de dados de pets com o dado ContentValues

        if (values.size( ) == 0) {
            return 0;
        }

        if ((values.containsKey(PetEntry.COLUMN_PET_NAME) &&
                !values.getAsString(PetEntry.COLUMN_PET_NAME).isEmpty( )) ||

                (values.containsKey(PetEntry.COLUMN_PET_WEIGHT) &&
                        values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT) > 0 &&
                        values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT) != null) ||

                values.containsKey(PetEntry.COLUMN_PET_GENDER) ||
                values.containsKey(PetEntry.COLUMN_PET_BREED)) {

            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }

            getContext().getContentResolver().notifyChange(uri, null);
            // Retorna o número de registros que foram afetados
            return db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
        } else {
            Log.e(LOG_TAG, "Updates inputs are invalid");
            Toast.makeText(getContext( ), "Não foi possivel atualizar", Toast.LENGTH_SHORT);
        }
        return 0;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs ) {
        // Obtém banco de dados com permissão de escrita
        SQLiteDatabase database = helper.getWritableDatabase( );

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Deleta todos os registros que correspondem ao selection e selection args
                int rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case PETS_ID:
                // Deleta um único registro dado pelo ID na URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType( Uri uri ) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}