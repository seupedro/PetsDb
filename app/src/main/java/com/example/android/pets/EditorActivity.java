/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    /** Stores URI from Intent */
    private Uri intentUri;

    /** Verify if user has enter some input */
    private boolean mPetHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener( ) {
        @Override
        public boolean onTouch( View v, MotionEvent event ) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        intentUri = getIntent().getData();
        if (intentUri == null){
            setTitle("Add a new Pet");
        } else {
            setTitle("Edit Pet");
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        getLoaderManager().initLoader(0, null, this);

    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        if (intentUri != null){
            int rowsDeleted = getContentResolver().delete(
                    intentUri,
                    null,
                    null
            );

            // Mostra uma mensagem toast dependendo se ou não o delete foi bem sucedido.
            if (rowsDeleted == 0) {
                // Se nenhum registro foi deletado, então houve um erro com o delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Caso contrário, o delete foi bem sucedido e podemos mostrar um toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
            getContentResolver().notifyChange(intentUri, null);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // Se o pet não mudou, continue lidando com clique do botão "back"
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Caso contrário, se houver alterações não salvas, configure uma caixa de diálogo para alertar o usuário.
        // Crie um click listener para lidar com o usuário, confirmando que mudanças devem ser descartadas.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicou no botão "Discard", fecha a activity atual.
                        finish();
                    }
                };

        // Mostra o diálogo que diz que há mudanças não salvas
        showUnsavedChangeDialog(discardButtonClickListener);
    }

    private void showUnsavedChangeDialog(
            DialogInterface.OnClickListener discardButtonClickListener ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your changes weren't save yet. Would you discart it?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener( ) {
            @Override
            public void onClick( DialogInterface dialog, int which ) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (intentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveOrUpdatePet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Se o pet não mudou, continua navegando para cima, para a activity pai
                // que é a {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Caso contrário, se houver alterações não salvas, configura um diálogo para alertar o usuário.
                // Cria um click listener para lidar com o usuário, confirmando que
                // mudanças devem ser descartadas.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Usuário clidou no botão "Discard", e navegou para a activity pai.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Mostra um diálogo que notifica o usuário de que há alterações não salvas
                showUnsavedChangeDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveOrUpdatePet() {

        Uri newUri;

        if (intentUri == null &&
                TextUtils.isEmpty(mNameEditText.getText().toString().trim()) && TextUtils.isEmpty(mBreedEditText.getText().toString().trim()) &&
                TextUtils.isEmpty(mWeightEditText.getText().toString().trim()) && mGender == PetEntry.GENDER_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        /** Initialize ContentValues e put data */
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, mNameEditText.getText().toString().trim());
        values.put(PetEntry.COLUMN_PET_BREED, mBreedEditText.getText().toString().trim());
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        if (!TextUtils.isEmpty(mWeightEditText.getText().toString().trim())){
            values.put(PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString().trim()));
        } else {
            values.put(PetEntry.COLUMN_PET_WEIGHT, 0);
        }

        if (intentUri == null ){
           // Insere um novo pet no provider, returnando o URI de conteúdo para o novo pet.
           newUri = getContentResolver( ).insert(PetEntry.CONTENT_URI, values);
           Log.d(LOG_TAG, String.valueOf(newUri));

           if (newUri == null) {
               // Se o novo conteúdo do URI é nulo, então houve um erro com inserção.
               Toast.makeText(this, R.string.insert_failed,
                       Toast.LENGTH_SHORT).show();
           } else {
               // Caso contrário, a inserção foi bem sucedida e podemos mostrar um toast.
               Toast.makeText(this, R.string.insert_ok,
                       Toast.LENGTH_SHORT).show();
               Log.e(LOG_TAG, newUri.toString());
           }
       } else {
           // Atualiza um novo pet no provider, returnando o URI de conteúdo para o novo pet.
          int update =  getContentResolver().update(intentUri, values, null, null);

          if(update == 1) {
              Toast.makeText(this, "Pet Salvo" , Toast.LENGTH_SHORT).show();
          } else
              Toast.makeText(this, "Atualização falhou", Toast.LENGTH_SHORT).show();
       }

        /** Close Activity */
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader( int id, Bundle args ) {

        if (intentUri != null){
            return new CursorLoader(
                    this,
                    intentUri,
                    null,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished( Loader <Cursor> loader, Cursor cursor ) {
        if (cursor == null ){
            return;
        }
        if (cursor.moveToNext( )) {
            mNameEditText.setText(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME)));
            mBreedEditText.setText(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED)));
            mWeightEditText.setText(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT)));
            mGenderSpinner.setSelection(
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER)));
        }
    }

    @Override
    public void onLoaderReset( Loader <Cursor> loader ) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGender = 0;
    }
}