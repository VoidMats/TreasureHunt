package com.example.mats.treasurehunt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.example.mats.treasurehunt.MainActivity.DEBUG;
import static com.example.mats.treasurehunt.Constants.USER_ID;
import static com.example.mats.treasurehunt.Constants.USER;

public class SearchActivity extends AppCompatActivity implements
        View.OnClickListener {

    /**
     * PATH_ID = Key for active/selected path
     * TIME = Key for active time
     * NODE_NUMBER = Key for which index/node the player are at
     */
    private static final String[] BUNDLE_KEYS = {
            "SELECTED_VALUE",
            "COUNTRY_LIST",
            "LOCATION_LIST",
            "TYPE_LIST",
            "RESULT_LIST"
    };


    //private static final boolean DEBUG = false;
    private static final String TAG = "LogActivitySearch";

    // Local data
    private String userId;
    private ArrayList<String> mCountryList ;
    private ArrayList<String> mLocationList;
    private ArrayList<String> mTypeList;
    private ArrayList<ListData> mResultList;
    private String mSelectedValue;

    // UI variables
    private Button buttonBack;
    private Spinner spCountry;
    private Spinner spLocation;
    private Spinner spType;
    private ListView lwSearchResult;
    private TextView tvSearchResult;

    private ArrayAdapter<String> arrayAdapterCountry;
    private ArrayAdapter<String> arrayAdapterLocation;
    private ArrayAdapter<String> arrayAdapterType;
    private SearchResultAdapter mArrayAdapterResult;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get data from MainActivity
        Intent obj = getIntent();
        userId = obj.getStringExtra(USER_ID);

        setContentView(R.layout.activity_search);

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate() <-- Collect bundle data.");
            mSelectedValue = savedInstanceState.getString(BUNDLE_KEYS[0]);
            mCountryList = savedInstanceState.getStringArrayList(BUNDLE_KEYS[1]);
            mLocationList = savedInstanceState.getStringArrayList(BUNDLE_KEYS[2]);
            mTypeList = savedInstanceState.getStringArrayList(BUNDLE_KEYS[3]);
            mResultList = savedInstanceState.getParcelableArrayList(BUNDLE_KEYS[4]);
        }
        else {
            // Create utilities
            mResultList = new ArrayList<>();
            mCountryList = new ArrayList<>();
            getCountry();
            mLocationList = new ArrayList<>();
            mTypeList = new ArrayList<>();
            mSelectedValue = "";
        }

        // Create ArrayAdapters
        arrayAdapterCountry = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mCountryList
        );
        arrayAdapterLocation = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mLocationList
        );
        arrayAdapterType = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mTypeList
        );
        mArrayAdapterResult = new SearchResultAdapter(this,mResultList);

        // Setup UI of the Activity
        buttonBack = (Button) findViewById(R.id.bSearchBack);
        buttonBack.setOnClickListener(this);
        spCountry = (Spinner) findViewById(R.id.spSearchCountry);
        spCountry.setAdapter(arrayAdapterCountry);
        spLocation = (Spinner) findViewById(R.id.spSearchLocation);
        spLocation.setAdapter(arrayAdapterLocation);
        spType = (Spinner) findViewById(R.id.spSearchType);
        spType.setAdapter(arrayAdapterType);
        lwSearchResult = (ListView) findViewById(R.id.lwSearch);
        lwSearchResult.setAdapter(mArrayAdapterResult);
        tvSearchResult = (TextView) findViewById(R.id.tvSearchSelected);

        // Setup country list
        spCountry.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "CountrySpinner <-- onItemSelected() has been triggered");
                if (position != 0 ) {
                    // Open location and get locations for this country
                    spLocation.setEnabled(true);
                    getLocation();
                    arrayAdapterLocation.notifyDataSetChanged();
                    // Disable type, but should already be disabled
                    spType.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spLocation.setEnabled(false);
            }
        });

        // Setup location list
        arrayAdapterLocation.notifyDataSetChanged();
        spLocation.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "LocationSpinner <-- onItemSelected() has been triggered");
                if (position != 0) {
                    // Save selected value
                    int selectedLocation = spLocation.getSelectedItemPosition();
                    SharedPreferences sharedPref = getSharedPreferences("Location",0);
                    SharedPreferences.Editor prefEditor = sharedPref.edit();
                    prefEditor.putInt("spSelectedLocation",selectedLocation);
                    prefEditor.commit();
                    // Open type and get valid options
                    spType.setEnabled(true);
                    getHuntlist();
                    //lwSearchResult.setSelection(0); // hmm
                    //mArrayAdapterResult.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spType.setEnabled(false);
            }
        });
        // Retrieve selected value
        SharedPreferences sharedPrefLocation = getSharedPreferences("Location",MODE_PRIVATE);
        int spValueLocation = sharedPrefLocation.getInt("spSelectedLocation",-1);
        if(spValueLocation != -1) {
            spLocation.setSelection(spValueLocation);
        }


        // Setup type list
        mTypeList.clear();
        mTypeList.add("");
        mTypeList.add("Sightseeing");
        mTypeList.add("Town");
        mTypeList.add("Quiz");
        mTypeList.add("Hunt");
        mTypeList.add("Education");
        Collections.sort(mTypeList);
        arrayAdapterType.notifyDataSetChanged();
        spType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "TypeSpinner <-- onItemSelected() has been triggered");
                if (position != 0) {
                    // Get data and filter out type
                    //getHuntlist();
                    String search = spType.getItemAtPosition(position).toString();
                    ArrayList<ListData> searchList = new ArrayList<>();
                    for( ListData tmp : mResultList) {
                        String checkedType = tmp.getType();
                        if ( checkedType.equals(search) ) {
                            searchList.add(tmp);
                        }
                    }
                    mResultList = searchList;
                    mArrayAdapterResult.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Add listeners
        lwSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListData tmp = mArrayAdapterResult.getItem(position);
                mSelectedValue = tmp.getId();
                tvSearchResult.setText(mSelectedValue);
            }
        });


        // Disable spinners and edittext to force user choose country and location
        if (spCountry.getSelectedItemPosition() == 0) {
            spLocation.setEnabled(false);
            //spType.setEnabled(false);
        }
        if (spLocation.getSelectedItemPosition() == 0 || mLocationList.size() == 0) {
            spType.setEnabled(false);
        }
        else {
            spLocation.setEnabled(true);
            spType.setEnabled(true);
        }

        // Update UI
        arrayAdapterCountry.notifyDataSetChanged();
        arrayAdapterLocation.notifyDataSetChanged();
        arrayAdapterType.notifyDataSetChanged();
        mArrayAdapterResult.notifyDataSetChanged();
        tvSearchResult.setText(mSelectedValue);
    }

    // *** InstanceState data handling ***
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() <-- Saved bundle data.");

        outState.putString(BUNDLE_KEYS[0], mSelectedValue);
        outState.putStringArrayList(BUNDLE_KEYS[1], mCountryList);
        outState.putStringArrayList(BUNDLE_KEYS[2], mLocationList);
        outState.putStringArrayList(BUNDLE_KEYS[3], mTypeList);
        outState.putParcelableArrayList(BUNDLE_KEYS[4], mResultList);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState() <-- Get bundle data.");

        // Get bundle data check
        mSelectedValue = savedInstanceState.getString(BUNDLE_KEYS[0]);
        mCountryList = savedInstanceState.getStringArrayList(BUNDLE_KEYS[1]);
        mLocationList = savedInstanceState.getStringArrayList(BUNDLE_KEYS[2]);
        mTypeList = savedInstanceState.getStringArrayList(BUNDLE_KEYS[3]);
        mResultList = savedInstanceState.getParcelableArrayList(BUNDLE_KEYS[4]);
    }

    // *** Listener and events ***
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bSearchBack:
                Intent obj =  new Intent(getBaseContext(), MainActivity.class);
                try{
                    if (DEBUG) {
                        mSelectedValue = "";   //Constant
                    }
                    obj.putExtra("PATH_ID", mSelectedValue);
                    setResult(Activity.RESULT_OK, obj);
                }
                catch(Exception e) {
                    Log.w(TAG, "onClick() method. Error pathId " + e.getMessage());
                    obj.putExtra("PATH_ID", "");
                    setResult(Activity.RESULT_CANCELED, obj);
                }
                finish();
                break;
            default:
                Log.w(TAG,"onClick() method has reached default");
                break;
        }
    }

    /**
     *  Get data from the Firebase server which country its possible to search for. This
     *  only necessary in the onCreate() function.
     */
    private void getCountry() {
        mCountryList.clear();
        mCountryList.add("-- Please Select Country --");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Country");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getCountry().onDataChanged() has been triggered");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds != null) {
                            mCountryList.add(ds.getKey());
                        }
                    }
                    Collections.sort(mCountryList);
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

    /**
     *  Get data from Firebase server which options there is for selected country. This is called
     *  each time a new country is selected.
     */
    private void getLocation() {
        mLocationList.clear();
        mLocationList.add("-- Please Select Location --");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Location");
        String country = spCountry.getSelectedItem().toString();
        ref.child(country).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getLocation().onDataChanged() has been triggered");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds != null) {
                            mLocationList.add(ds.getKey().toString());
                        }
                    }
                } else {
                    Log.d(TAG, "getCountry().onDataChanged() result into  is null");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });
        Collections.sort(mLocationList);  // <-- Case sensitive
        arrayAdapterLocation.notifyDataSetChanged();
    }

    private void getHuntlist() {
        mResultList.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Place");
        String location = spLocation.getSelectedItem().toString();
        ref.child(location).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getHuntlist().onDataChanged() has been triggered");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds != null) {
                            String[] splitStr = ds.getValue().toString().split(",");
                            List<String> str = Arrays.asList(splitStr);
                            if (str.size() != 4) {
                                int dif = 4 - str.size();
                                if( dif > 0 ) {
                                    while( dif >= 0) {
                                        str.add("");
                                        dif--;
                                    }
                                }
                                else {
                                    while( dif <=0 ) {
                                        str.remove(str.size()-1);
                                        dif++;
                                    }
                                }
                            }
                            String tmp_nodes = str.get(1);
                            tmp_nodes = tmp_nodes.replaceAll("\\D+","");
                            String tmp_time = str.get(2);
                            tmp_time = tmp_time.replaceAll("\\D+","");
                            int checkint = Integer.parseInt(tmp_nodes);
                            long checklong = Long.parseLong(tmp_time);
                            ListData tmp = new ListData(str.get(0),checkint,
                                    checklong,str.get(3), ds.getKey());
                            mResultList.add(tmp);

                        }
                    }
                    mArrayAdapterResult.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    /*
    public String searchHunt() {
        String returnHuntId = "";

        // Connect to Firebase and get values from user
        // SELECT * FROM Hunt WHERE location = "_location"
        Query q = FirebaseDatabase.getInstance().getReference("Hunt").child(spLocation.getSelectedItem().toString())
                .orderByChild()
                .equalTo(_freeText.toLowerCase());

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange has been triggered");
                if( dataSnapshot.exists() ) {
                    for( DataSnapshot ds : dataSnapshot.getChildren() ) {
                        HuntData hunt = ds.getValue(HuntData.class);
                        huntList.add(hunt);
                    }
                    // TODO Make an Adapter which will take care about the listening of items
                    for( HuntData hunt : huntList ) {
                        String tmp = hunt.getName() + ", " + hunt.getLocation() + ", " + hunt.getId();
                        showList.add(tmp);
                    }
                    arrayAdapterResult.notifyDataSetChanged();
                }
                else
                    Log.d(TAG, "HuntData is null");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });

        return  returnHuntId;
    }  */
}
