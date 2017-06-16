package com.example.dell.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.pets.data.PetdbHelper;
import com.example.dell.pets.data.PetsContract.PetsEntry;

import static android.R.attr.name;
import static android.R.id.edit;

public class CatalogActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

private static final int PET_LOADER = 0;
    PetCursorAdapter petCursorAdapter;
    ListView cursorlist;
    PetdbHelper mDBhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editdetails = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(editdetails);


            }
        });

        cursorlist = (ListView) findViewById(R.id.list);
        //set the empty view for the list
        View emptyView = findViewById(R.id.empty_view);
        cursorlist.setEmptyView(emptyView);

        petCursorAdapter = new PetCursorAdapter(this,null);
        cursorlist.setAdapter(petCursorAdapter);
         mDBhelper = new PetdbHelper(this);

        //set the  pet click listener
        cursorlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Uri currentpeturi = ContentUris.withAppendedId(PetsEntry.CONTENT_URI,id);

                Intent editpet = new Intent(CatalogActivity.this, EditorActivity.class);
                editpet.setData(currentpeturi);

                startActivity(editpet);
            }
        });


        //kick of the loader
        getSupportLoaderManager().initLoader(PET_LOADER,null,this);
       // getLoaderManager().initLoader(PET_LOADER,null,this);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //this creates the menu options which have insert dummy data options and etc
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insert();
                return true;
            case R.id.action_delete_all_entries:
                showConfirmDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insert() {
        Intent i = new Intent(CatalogActivity.this, EditorActivity.class);
        startActivity(i);
    }



    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //projection specifies the name's of the columns to query for.
        String[] Projection = {
                PetsEntry._ID,
                PetsEntry.COLUMN_PET_NAME,
                PetsEntry.COLUMN_PET_BREED,
                PetsEntry.COLUMN_PET_GENDER,
                PetsEntry.COLUMN_PET_WEIGHT};

        return new CursorLoader(this,PetsEntry.CONTENT_URI,
                Projection, null, null,null);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
    petCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
    petCursorAdapter.swapCursor(null);
    }


    //deleting all the pets at once
    private void showConfirmDeleteDialog(){
        //alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.confirm_delete_all_pets);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePet();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {

        int rowsdeleted = getContentResolver().delete(PetsEntry.CONTENT_URI,null,null);
        if(rowsdeleted == 0){
            Toast.makeText(CatalogActivity.this,R.string.delete_pet_fail_text,Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(CatalogActivity.this,R.string.delete_pet_success_text,Toast.LENGTH_SHORT).show();

        }
    }
}