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

import static com.example.mats.treasurehunt.Constants.HUNT;
import static com.example.mats.treasurehunt.Constants.NULL_PATH;
import static com.example.mats.treasurehunt.Constants.NULL_USER;
import static com.example.mats.treasurehunt.Constants.PATH_ID;
import static com.example.mats.treasurehunt.Constants.TIME_FINSIHED;
import static com.example.mats.treasurehunt.Constants.TIME_NEG;
import static com.example.mats.treasurehunt.Constants.USER;
import static com.example.mats.treasurehunt.Constants.USER_ID;
import static com.example.mats.treasurehunt.Constants.PLAY_MODE;
import static com.example.mats.treasurehunt.MainActivity.DEBUG;

public class FinishActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "LogActivityFinish";

    // Local data
    String mUserId = NULL_USER;
    String mPathId = NULL_PATH;
    TimeCalc mTime = new TimeCalc();
    long mTimeAvg = 0;
    UserData mUser = new UserData();
    HuntData mHunt = new HuntData();
    NetworkUtil networkUtility = new NetworkUtil();

    // UI variables
    TextView tvTotalTime;
    TextView tvAvgTime;
    TextView tvBestTime;
    Button bBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get data from Intent
        Intent obj = getIntent();
        if (obj != null) {
            mUserId = obj.getStringExtra(USER_ID);
            mPathId = obj.getStringExtra(PATH_ID);
            mTime.setMs(obj.getLongExtra(TIME_FINSIHED, 0));
            mTime.setNeg(obj.getBooleanExtra(TIME_NEG, false));
        }

        setContentView(R.layout.activity_finish);

        if (savedInstanceState != null) {
            mUserId = savedInstanceState.getString(USER_ID);
            mPathId = savedInstanceState.getString(PATH_ID);
            mTime.setMs(savedInstanceState.getLong(TIME_FINSIHED));
            mTime.setNeg(savedInstanceState.getBoolean(TIME_NEG));
            mUser = savedInstanceState.getParcelable(USER);
            mHunt = savedInstanceState.getParcelable(HUNT);
        }

        // Setup UI of the activity
        tvTotalTime = findViewById(R.id.tvFinishTotalTimeScr);
        tvAvgTime = findViewById(R.id.tvFinishAvgScr);
        tvBestTime = findViewById(R.id.tvFinishBestTimeScr);
        bBack = findViewById(R.id.bFinishBack);
        bBack.setOnClickListener(this);

        // Check network
        networkUtility.checkNetworkAvailable(getApplicationContext());

        // Get data
        getHuntData();
        getUserdata();
    }

    // *** InstanceState data handling ***
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(USER_ID, mUserId);
        outState.putString(PATH_ID, mPathId);
        outState.putLong(TIME_FINSIHED, mTime.getMs());
        outState.putBoolean(TIME_NEG, mTime.getNeg());
        outState.putParcelable(USER, mUser);
        outState.putParcelable(HUNT, mHunt);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mUserId = savedInstanceState.getString(USER_ID);
        mPathId = savedInstanceState.getString(PATH_ID);
        mTime.setMs(savedInstanceState.getLong(TIME_FINSIHED));
        mTime.setNeg(savedInstanceState.getBoolean(TIME_NEG));
        mUser = savedInstanceState.getParcelable(USER);
        mHunt = savedInstanceState.getParcelable(HUNT);
    }

    // *** Listeners and events ****
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bFinishBack:
                Intent obj = new Intent(getBaseContext(), MainActivity.class);
                try {
                    if (!DEBUG && mUserId != NULL_USER) {
                        saveUserData();
                    }
                    setResult(Activity.RESULT_OK, obj);
                }
                catch(Exception e) {
                    Log.w(TAG, "onClick() <-- Error pathId" + e.getMessage());
                    setResult(Activity.RESULT_CANCELED, obj);
                }
                finish();
                break;
            default:
                Log.w(TAG, "onClick() method has reached default");
                break;
        }
    }

    // *** UI methods ***
    private void showData() {
        tvTotalTime.setText(mTime.getTimeString());
        tvTotalTime.invalidate();
        tvAvgTime.setText(mTime.getTimeString(mTimeAvg));
        tvAvgTime.invalidate();
        tvBestTime.setText(mTime.getTimeString(mHunt.getTime()));
        tvBestTime.invalidate();
    }

    // *** Firebase methods ***
    private void saveUserData() {
        if (networkUtility.checkNetWorkAvailable()) {
            UserData tmpUser = new UserData(mUser);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(mUserId).setValue(tmpUser);
        }

    }


    private void getUserdata() {
        // If the user is not logged in the data will not be received
        if (!mUserId.equals(NULL_USER)) {
            if ( networkUtility.checkNetworkAvailable(getApplicationContext()) ) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(mUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot ds) {
                        if( ds.exists() ) {
                            mUser = ds.getValue(UserData.class);
                            calcScore();
                            showData();
                        }
                        else
                            Log.d(TAG, "getUserdata().onDataChanged() <-- result data is null");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
                    }
                });
            }
            else {
                Toast.makeText(this, networkUtility.errorMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveHuntData() {
        // If the user is not logged in the data will not be saved
        if  (networkUtility.checkNetWorkAvailable() && mUserId != NULL_USER) {
            HuntData tmpHunt = new HuntData(mHunt);
        }

    }

    private void getHuntData() {
        if (networkUtility.checkNetworkAvailable(getApplicationContext()) &&
            !mPathId.equals(NULL_PATH) ) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Hunts");
            ref.child(mPathId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getHuntPath.onDataChanged() has been triggered");
                    if (dataSnapshot.exists()) {
                        mHunt = new HuntData(dataSnapshot.getValue(HuntData.class));
                        calcAvg();
                        calcbest();
                        showData();
                    } else {
                        Log.d(TAG, "getHuntData.onDataChanged() <-- result in data is null");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
                }
            });
        }
        else {
            Toast.makeText(this, networkUtility.errorMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // *** Calculation methods ***
    private void calcScore() {
        int result = 0;
        PLAY_MODE mode = PLAY_MODE.values()[mHunt.getType()];
        switch(mode) {
            case DEFAULT:
                // Error
                break;
            case QUIZ:
                result = mUser.getScoreQuiz();
                result += mTime.getMs()/1000;
                mUser.setScoreQuiz(result);
                mUser.setQuiz(mUser.getQuiz()+1);
                break;
            case HUNT:
                result = mUser.getScoreHunt();
                result += mTime.getMs()/1000;
                mUser.setScoreHunts(result);
                mUser.setHunts(mUser.getHunts()+1);
                break;
            default:
                Log.d(TAG, "Selected type is not supported");
                break;
        }
    }

    private void calcAvg() {
        if (mHunt.getNoNodes() != 0) {
            if (mTime.getNeg()) {
                mTimeAvg = (mHunt.getTotaltime() + mTime.getMs())/mHunt.getNoNodes();
            }
            else {
                mTimeAvg = (mHunt.getTotaltime() - mTime.getMs())/mHunt.getNoNodes();
            }
        }
    }

    private void calcbest() {
        if (mTime.getMs() > mHunt.getTime() && mTime.getNeg() == false) {
            mHunt.setTime(mTime.getMs());
        }
    }
}
