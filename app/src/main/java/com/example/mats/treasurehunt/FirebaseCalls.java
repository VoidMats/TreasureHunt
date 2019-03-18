package com.example.mats.treasurehunt;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.mats.treasurehunt.MainActivity.DEBUG;



public class FirebaseCalls {

    private static final String TAG = "LogClassFirebaseCalls: ";

    private NetworkUtil mNetWorkUtil = new NetworkUtil();
    private ArrayList<String> mCountryList;

    public FirebaseCalls(Context _context) {

        if (mNetWorkUtil.checkNetworkAvailable(_context) ) {
            getCountryList();
        }
    }

    private void getCountryList() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Country");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (DEBUG) Log.d(TAG, "getCountryList().onDataChanged() has been triggered");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds != null) {
                            mCountryList.add(ds.getKey());
                        }
                    }
                } else {
                    Log.d(TAG, "getCountryList().onDataChanged() result into  is null");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });
    }


}
