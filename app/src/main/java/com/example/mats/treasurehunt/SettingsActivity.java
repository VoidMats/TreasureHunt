package com.example.mats.treasurehunt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import static com.example.mats.treasurehunt.Constants.NULL_PATH;
import static com.example.mats.treasurehunt.Constants.NULL_USER;
import static com.example.mats.treasurehunt.Constants.SETTING_RESULT_NAME;
import static com.example.mats.treasurehunt.Constants.SETTING_RESULT_REVOKE;
import static com.example.mats.treasurehunt.MainActivity.DEBUG;
import static com.example.mats.treasurehunt.Constants.USER_ID;
import static com.example.mats.treasurehunt.Constants.USER;

public class SettingsActivity extends AppCompatActivity implements
        View.OnClickListener {


    private static final String TAG = "LogActivitySettings";

    // Local data in the activity
    String mUserID;
    UserData mUser = new UserData();
    List<String> listLanguage = new ArrayList<String>();
    NetworkUtil networkUtility = new NetworkUtil();
    // UI variables
    Button bSave;
    Button bRevoke;
    EditText etAccount;
    EditText etEmail;
    Spinner spLanguage;
    EditText etLocation;
    EditText etName;
    EditText etPhoto;

    private ArrayAdapter<String> arrayAdapterCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /// Check Network
        networkUtility.checkNetworkAvailable(getApplicationContext());

        // Get data from MainActivity
        Intent obj = getIntent();
        mUserID = obj.getStringExtra(USER_ID);
        if (DEBUG) mUserID = NULL_USER;

        if (savedInstanceState != null) {
            mUserID = savedInstanceState.getString(USER_ID);
            mUser = savedInstanceState.getParcelable(USER);
        }

        // Create arrayAdapters
        if (networkUtility.checkNetWorkAvailable()) {
            getCountry();
        }
        else {
            // Place default values
            listLanguage.add("Sweden");
            listLanguage.add("England");
        }
        arrayAdapterCountry = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listLanguage
        );
        //simple_spinner_dropdown_item,


        // Setup of the Activity
        bSave = (Button) findViewById(R.id.bSettingsSave);
        bSave.setOnClickListener(this);
        bRevoke = (Button) findViewById(R.id.bSettingsRevoke);
        bRevoke.setOnClickListener(this);
        etAccount = (EditText) findViewById(R.id.tSettingsAccountScr);
        etEmail = (EditText) findViewById(R.id.etSettingsEmailScr);
        spLanguage = (Spinner) findViewById(R.id.spSettingsLanguage);
        spLanguage.setAdapter(arrayAdapterCountry);
        etLocation = (EditText) findViewById(R.id.etSettingsLocation);
        etName = (EditText) findViewById(R.id.etSettingsName);
        etPhoto = (EditText) findViewById(R.id.etSettingsPhotoSrc);

        // Setup language list
        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "LanguageSpinner <-- onItemSelected() has been triggered");
                if (position != 0) {
                    int pos = spLanguage.getSelectedItemPosition();
                    String tmp = arrayAdapterCountry.getItem(pos);
                    mUser.setLanguage(tmp);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Get user data from Firebase
        getDataFromFirebase();

        // Disable does facts which user can't change
        etEmail.setEnabled(false);
        etAccount.setEnabled(false);
    }

    // *** InstanceState data handling ***
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(USER_ID, mUserID);
        outState.putParcelable(USER, mUser);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Get bundle data
        mUserID = savedInstanceState.getString(USER_ID);
        mUser = savedInstanceState.getParcelable(USER);
    }

    // *** Listeners and events ***
    @Override
    public void onClick(View v) {
        Intent obj =  new Intent(getBaseContext(), MainActivity.class);
        switch (v.getId()) {
            case R.id.bSettingsSave:
                saveDataToFirebase();
                obj.putExtra(SETTING_RESULT_REVOKE, false);
                obj.putExtra(SETTING_RESULT_NAME, mUser.getUsername());
                setResult(Activity.RESULT_OK, obj);
                break;
            case R.id.bSettingsRevoke:
                //AlertDialog dlg = deleteUserDialog();
                //dlg.show();
                // TODO Alert dialog as callback - interface
                obj.putExtra(SETTING_RESULT_REVOKE, true);
                obj.putExtra(SETTING_RESULT_NAME, mUser.getUsername());
                removeDataFromFirebase();
                setResult(Activity.RESULT_OK, obj);
                break;
        }
        finish();
    }

    // *** Private methods and firebase handling ***
    private void saveDataToFirebase() {
        UserData tmpUser = new UserData(mUser);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        tmpUser.setAccount(etAccount.getText().toString());
        tmpUser.setLocation(etLocation.getText().toString());
        tmpUser.setPhoto("Place photo url");
        tmpUser.setUsername(etName.getText().toString());
        //NB!!  User language already set because its always updated in mUser

        ref.child(mUserID).setValue(tmpUser);
    }

    // Connect to Firebase and remove user
    private void removeDataFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mUserID).removeValue();
    }

    // Connect to Firebase and get settingsdata
    private void getDataFromFirebase() {
        // Connect to Firebase and get values from user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Add event listener to recive data from firebase
        ref.child(mUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                Log.d(TAG, "onCreate.onDataChanged() has been triggered");
                if( ds.exists() ) {
                    mUser = ds.getValue(UserData.class);
                    Log.d(TAG, mUser.getUsername());
                    // Load data to view
                    int position = arrayAdapterCountry.getPosition(mUser.getLanguage());
                    spLanguage.setSelection(position);
                    arrayAdapterCountry.notifyDataSetChanged();
                    etAccount.setText(mUser.getAccount());
                    etEmail.setText(mUser.getEmail());
                    etLocation.setText(mUser.getLocation());
                    etName.setText(mUser.getUsername());
                    etPhoto.setText(mUser.getPhoto());
                }
                else
                    Log.d(TAG, "onDataChanged() result into user data is null");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });
    }

    private void getCountry() {
        listLanguage.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Country");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getCountry().onDataChanged() has been triggered");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds != null) {
                            listLanguage.add(ds.getKey());
                        }
                    }
                    Collections.sort(listLanguage);
                } else {
                    Log.d(TAG, "getCountry().onDataChanged() result into  is null");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });
    }

    private AlertDialog deleteUserDialog()
    {
        AlertDialog deleteDialogBox = new AlertDialog.Builder(this)
                .setTitle(R.string.tSettingAlertTitle)
                .setMessage(R.string.tSettingAlertMessage)
                .setIcon(R.drawable.ic_baseline_block_24px)
                .setPositiveButton(R.string.tConfirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.tDecline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return deleteDialogBox;
    }
}
