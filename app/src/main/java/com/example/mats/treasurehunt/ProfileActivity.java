package com.example.mats.treasurehunt;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.mats.treasurehunt.Constants.USER;
import static com.example.mats.treasurehunt.Constants.USER_ID;
import static com.example.mats.treasurehunt.MainActivity.DEBUG;

public class ProfileActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "LogActivityProfile";

    // Local dat in activity
    String mUserID;
    UserData mUser = new UserData();

    // UI variables
    Button bBack;
    TextView tvNameScr;
    TextView tvlocationScr;
    TextView tvQuizScr;
    TextView tvHuntsScr;
    TextView tvAvgQuizScr;
    TextView tvAvgHuntsScr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get data from MainActivity
        Intent obj = getIntent();
        mUserID = obj.getStringExtra(USER_ID);
        if (DEBUG) mUserID = "Test";

        if (savedInstanceState != null) {
            mUserID = savedInstanceState.getString(USER_ID);
            mUser = savedInstanceState.getParcelable(USER);
        }

        // Setup of the Activity
        bBack = (Button) findViewById(R.id.bProfileBack);
        bBack.setOnClickListener(this);
        tvNameScr = (TextView) findViewById(R.id.tProfileNameScr);
        tvlocationScr = (TextView) findViewById(R.id.tProfileLocationScr);
        tvQuizScr = (TextView) findViewById(R.id.tProfileQuizScr);
        tvHuntsScr = (TextView) findViewById(R.id.tProfileHuntsScr);
        tvAvgQuizScr = (TextView) findViewById(R.id.tProfileScoreQuizScr);
        tvAvgHuntsScr = (TextView) findViewById(R.id.tProfileScoreHuntScr);

        // Load data to Activity
        getDataFromFirebase();
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

        mUserID = savedInstanceState.getString(USER_ID);
        mUser = savedInstanceState.getParcelable(USER);
    }

    @Override
    public void onClick(View v) {
        Intent obj =  new Intent(getBaseContext(), MainActivity.class);
        switch (v.getId()) {
            case R.id.bProfileBack:
                setResult(Activity.RESULT_OK, obj);
                finish();
                break;
            default:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }

    }

    // Connect to Firebase and get settingsdata
    private void getDataFromFirebase() {
        // Connect to Firebase and get values from user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // Add event listener to recive data from firebase
        ref.child(mUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                Log.d(TAG, "getDataFromFirebase().onDataChanged() has been triggered");
                if( ds.exists() ) {
                    mUser = ds.getValue(UserData.class);
                    tvNameScr.setText(mUser.getUsername());
                    tvlocationScr.setText(mUser.getLocation());
                    tvQuizScr.setText( String.valueOf(mUser.getQuiz()) );
                    tvHuntsScr.setText( String.valueOf(mUser.getHunts()) );
                    float avgQuiz = calculateAvg(mUser.getScoreQuiz(), mUser.getQuiz());
                    float avgHunts = calculateAvg(mUser.getScoreHunt(), mUser.getHunts());
                    tvAvgQuizScr.setText(String.valueOf(avgQuiz));
                    tvAvgHuntsScr.setText(String.valueOf(avgHunts));
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

    private float calculateAvg(int _score, int _noGames) {
        if (_noGames <= 0) {
            return 0;
        }
        else {
            return _score/_noGames;
        }
    }
}
