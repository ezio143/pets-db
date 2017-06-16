package com.example.dell.pets;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dell.pets.data.PetsContract;

import org.w3c.dom.Text;

import java.util.zip.Inflater;

/**
 * Created by DELL on 03-03-2017.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor c, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.cursor_listview,parent,false);
    }

    @Override
    public void bindView(View view, Context c, Cursor cursor) {

        TextView nametext  = (TextView) view.findViewById(R.id.text_name);
        String name = cursor.getString(cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_NAME));

        nametext.setText(name);

        TextView breedtext = (TextView) view.findViewById(R.id.text_breed);
        String breed = cursor.getString(cursor.getColumnIndexOrThrow(PetsContract.PetsEntry.COLUMN_PET_BREED));
        if(!TextUtils.isEmpty(breed))
        breedtext.setText(breed);
        else
            breedtext.setText(R.string.unknown_breed);

    }
}
