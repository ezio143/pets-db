package com.example.dell.pets;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dell.pets.data.PetdbHelper;
import com.example.dell.pets.data.PetsContract.PetsEntry;

import org.w3c.dom.Text;

import static android.os.Build.VERSION_CODES.M;
import static com.example.dell.pets.data.PetsContract.PetsEntry.GENDER_FEMALE;
import static com.example.dell.pets.data.PetsContract.PetsEntry.GENDER_MALE;
import static com.example.dell.pets.data.PetsContract.PetsEntry.GENDER_UNKNOWN;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PET_LOADER = 0;
    private EditText mNameEditText, mBreedEditText, mWeightEditText;
    private Spinner mGenderSpinner;
    private int mGender = GENDER_UNKNOWN;

    private SQLiteDatabase db;
    private PetdbHelper mDbHelper;
    Uri currentpeturi;
    private boolean Pethaschanged = false;


    private View.OnTouchListener   mTouchListener = new View.OnTouchListener(){
        public boolean onTouch(View view, MotionEvent motionEvent){
            Pethaschanged = true;
            return false;
        }
    };

    //getters and setters


    public EditText getmBreedEditText() {
        return mBreedEditText;
    }

    public void setmBreedEditText(EditText mBreedEditText) {
        this.mBreedEditText = mBreedEditText;
    }

    public int getmGender() {
        return mGender;
    }

    public void setmGender(int mGender) {
        this.mGender = mGender;
    }

    public EditText getmNameEditText() {
        return mNameEditText;
    }

    public void setmNameEditText(EditText mNameEditText) {
        this.mNameEditText = mNameEditText;
    }

    public EditText getmWeightEditText() {
        return mWeightEditText;
    }


    public void setmWeightEditText(EditText mWeightEditText) {
        this.mWeightEditText = mWeightEditText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentpeturi = intent.getData();
        //check the uri . if not null set bar to edit pet else set bar to add a pet

        if(currentpeturi == null){
            //this is a new pet
            setTitle(
                    getString(R.string.editor_activity_title_new_pet)
            );
            //hide the delete option
            invalidateOptionsMenu();
        }
        else{
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getSupportLoaderManager().initLoader(PET_LOADER,null,this);

        }



        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);


        setupSpinner();

    }//close saved instance method


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);

        //if its a new pet hide the delete option
        if(currentpeturi == null){
           MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    //Setup the dropdown spinner that allows the user to select the gender of the pet.
    private void setupSpinner() {
        //the new adapter is made up of (context,stringarray,library spinner list );
        //the spinner will have default layout defined by the android library
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender_options,
                android.R.layout.simple_spinner_item);

        //specify drop down list style with 1 line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        //we define a array adapter compatible with the spinner layout and string array
        //we the take the layout spinner and attach the adapter to it
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        //action to perform when gender list items are selected
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("Male")) {
                        mGender = GENDER_MALE;
                    } else if (selection.equals("Female")) {
                        mGender = GENDER_FEMALE;
                    } else {
                        mGender = GENDER_UNKNOWN; //unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mGender = GENDER_UNKNOWN;//by default unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                    savePet();
                //Exit activity
                finish();
                return true;
            case R.id.action_delete:
                if(currentpeturi != null) {
                    showDeleteConfirmationDialog();
                    return true;
                }
            case android.R.id.home:
                if(!Pethaschanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                //show unsaved changes dialog
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePet() {

        String name = mNameEditText.getText().toString().trim();
        String breed = mBreedEditText.getText().toString().trim();
        String stringweight = mWeightEditText.getText().toString().trim();

        int gender = mGender;

        //check for the empty fields
        if(currentpeturi == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) && TextUtils.isEmpty(stringweight)
                && mGender == PetsEntry.GENDER_UNKNOWN){
            return ;
        }
        int weight = 0;
        if(!TextUtils.isEmpty(stringweight)){
            weight = Integer.parseInt(stringweight);
        }

        //to insert the data into pets.db in a (key,value) fashion
        ContentValues values = new ContentValues();

        values.put(PetsEntry.COLUMN_PET_NAME, name);
        values.put(PetsEntry.COLUMN_PET_BREED, breed);
        values.put(PetsEntry.COLUMN_PET_GENDER, gender);
        values.put(PetsEntry.COLUMN_PET_WEIGHT, weight);

        if(currentpeturi == null) {

            Uri Id = getContentResolver().insert(PetsEntry.CONTENT_URI, values);
        }
        else{
            int updatedrows = getContentResolver().update(currentpeturi,values,null,null);
            if(updatedrows == 0){
                Toast.makeText(EditorActivity.this,R.string.editor_update_pet_fail,Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(EditorActivity.this,R.string.editor_update_pet_pass,Toast.LENGTH_SHORT).show();
            }

        }
     }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] Projection = {
                PetsEntry._ID,
                PetsEntry.COLUMN_PET_NAME,
                PetsEntry.COLUMN_PET_BREED,
                PetsEntry.COLUMN_PET_GENDER,
                PetsEntry.COLUMN_PET_WEIGHT};

        return new CursorLoader(this,currentpeturi,
                Projection, null, null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //quit early if the cursor is null or less than 1 row in the cursor
        if(data == null || data.getCount()<1){
            return;
        }



        if(data.moveToFirst()) {

            mNameEditText.setText(data.getString(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_NAME)));
           mBreedEditText.setText(data.getString(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_BREED)));
           int weight = data.getInt(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_WEIGHT));
            mWeightEditText.setText(Integer.toString(weight));
           //get the gender
            int gender = data.getInt(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_GENDER));
            switch (gender){
                case PetsEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);break;
                case PetsEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);break;
                case PetsEntry.GENDER_UNKNOWN:
                    mGenderSpinner.setSelection(0);break;

            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);


    }

    //method for the discard dialog
    private void showUnsavedChangesDialog(
    DialogInterface.OnClickListener discardbuttonClickListener){

        //create an alertdialog and set the message and click listeners

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);

        builder.setPositiveButton(R.string.discard, discardbuttonClickListener);

        builder.setNegativeButton(R.string.keep_editing,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog ,int id){
                //user has clicked the keep editing button
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        //create and show the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //hook up the back button

    @Override
    public void onBackPressed() {
        if(!Pethaschanged) {
            super.onBackPressed();
            return;
        }

        //create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //close the current activity as user has clicked discard button
                finish();
            }
        };
        //show unsaved changes dialog
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    //show delete pet dialog
    private void showDeleteConfirmationDialog (){
        //create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletPet();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null ){
                    dialogInterface.dismiss();
                }
            }
        });

        //create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletPet() {

        //deleting the current pet using the currenturi
        int rowsdeleted = getContentResolver().delete(currentpeturi,null,null);
        if(rowsdeleted == 0){
            Toast.makeText(EditorActivity.this,R.string.delete_pet_fail_text,Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(EditorActivity.this,R.string.delete_pet_success_text,Toast.LENGTH_SHORT).show();

        }
        finish();

    }


}
