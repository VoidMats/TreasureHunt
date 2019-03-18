package com.example.mats.treasurehunt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.location.Geocoder;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.mats.treasurehunt.Constants.NULL_PATH;
import static com.example.mats.treasurehunt.Constants.NULL_USER;
import static com.example.mats.treasurehunt.Constants.PATH_ID;
import static com.example.mats.treasurehunt.Constants.TIME_FINSIHED;
import static com.example.mats.treasurehunt.Constants.TIME_NEG;
import static com.example.mats.treasurehunt.Constants.USER_ID;
import static com.example.mats.treasurehunt.Constants.PLAY_MODE;
import static com.example.mats.treasurehunt.MainActivity.SLOW_UPDATE_TIMER;
import static com.example.mats.treasurehunt.MainActivity.FAST_UPDATE_TIMER;
import static com.example.mats.treasurehunt.MainActivity.REMOVE_TIME;
import static com.example.mats.treasurehunt.MainActivity.DEBUG;
import static com.example.mats.treasurehunt.MainActivity.mUser;
import static com.example.mats.treasurehunt.MainActivity.mUserID;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.example.mats.treasurehunt.MainActivity.mTimerCalc;
import static com.example.mats.treasurehunt.MainActivity.mActiveHunt;
import static com.example.mats.treasurehunt.MainActivity.mNodeNumber;
import static com.example.mats.treasurehunt.MainActivity.mPath;


public class HuntFragment extends Fragment
        implements View.OnClickListener {

    // The fragment static const variables
    private enum REQUEST_CODE {
        FINISH_CODE
    }

    /**
     * ARG_USER_ID =
     * ARG_PATHID =
     * ARG_REQ_LOCATION_UPDATE =
     * ARG_DISTANCE =
     */
    private static final String[] ARG_KEYS_BUNDLE = {
            "ARG_USER_ID",              // String
            "ARG_PATHID",               // String
            "ARG_REQ_LOCATION_UPDATE",  // Boolean
            "ARG_DISTANCE",              // Float
            "ARG_CURRENT_NODE",          // Parcelable <-- NodeData
            "ARG_MEMORY_GPS"             //Boolean
    };
    private static final String TAG = "LogHuntFragment";
    private static final Boolean SHOW_GPS = true;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int DISTANCE_TRIGGER = 30;

    // Local variables
    private String mUserID = NULL_USER;
    private String mPathID = NULL_PATH;
    private NodeData mNode = new NodeData();
    private int mHintNumber;
    private boolean mLocationMemory = false;
    // Location and position
    private FusedLocationProviderClient mFusedLocationClient = null;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private boolean mRequestLocationUpdate = false;
    private double mCoordLati;
    private double mCoordLong;
    private float mDistance = 0.0f;
    // Timers and handlers
    private Timer mTimer = null;
    private TimerTask mTimerTaskGPS;
    private TimerTask mTimerTaskShowTime;
    private Handler mTimeHandlerGPS;
    private Handler mTimeHandlerShowTime;

    // UI variables
    private ImageButton ibAnswer;
    private ImageButton ibHint;
    private ImageView iwNavigation;
    private ImageView iwType;
    private ImageView iwImage;
    private TextView twQuestion;
    private EditText etAnswer;
    private TextView tvTime;

    // Listener
    private OnFragmentInteractionListener mListener;

    public HuntFragment() {
        // Required empty public constructor
    }

    /**
     * @param _userID [String]
     * @param _pathID [String]  .
     * @return A new instance of fragment HuntFragment.
     */
    public static HuntFragment newInstance(String _userID, String _pathID) {
        HuntFragment fragment = new HuntFragment();
        Bundle args = new Bundle();
        if ( _userID == null ) _userID = NULL_USER;
        args.putString(USER_ID, _userID);
        args.putString(PATH_ID, _pathID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Log.d(TAG, "onCreate() <-- Get bundle data in HuntFragment.");
            mUserID = getArguments().getString(USER_ID);
            mPathID = getArguments().getString(PATH_ID);

        }
        if (savedInstanceState != null)  {
            mUserID = savedInstanceState.getString(ARG_KEYS_BUNDLE[0]);
            mPathID = savedInstanceState.getString(ARG_KEYS_BUNDLE[1]);
            mRequestLocationUpdate = savedInstanceState.getBoolean(ARG_KEYS_BUNDLE[2]);
            mDistance = savedInstanceState.getFloat(ARG_KEYS_BUNDLE[3]);
            mNode = savedInstanceState.getParcelable(ARG_KEYS_BUNDLE[4]);
            mLocationMemory =savedInstanceState.getBoolean(ARG_KEYS_BUNDLE[5]);
        }

        // Local variables
        mCoordLati = 0d;
        mCoordLong = 0d;
        mRequestLocationUpdate = false;
        mFusedLocationClient = getFusedLocationProviderClient(getActivity());
        mTimeHandlerGPS = new Handler();
        mTimeHandlerShowTime = new Handler();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

    }

    // *** InstanceState data handling ***
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saved bundle data in HuntFragment");
        outState.putString(ARG_KEYS_BUNDLE[0], mUserID);
        outState.putString(ARG_KEYS_BUNDLE[1], mPathID);
        outState.putBoolean(ARG_KEYS_BUNDLE[2], mRequestLocationUpdate);
        outState.putFloat(ARG_KEYS_BUNDLE[3], mDistance);
        outState.putParcelable(ARG_KEYS_BUNDLE[4], mNode);
        outState.putBoolean(ARG_KEYS_BUNDLE[5], mLocationMemory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "HuntFragment trigger onCreateView()");

        if( savedInstanceState != null) {
            Log.d(TAG, "onCreateView() <-- Get bundle data in HuntFragment.");
            mUserID = savedInstanceState.getString(ARG_KEYS_BUNDLE[0]);
            mPathID = savedInstanceState.getString(ARG_KEYS_BUNDLE[1]);
            mRequestLocationUpdate = savedInstanceState.getBoolean(ARG_KEYS_BUNDLE[2]);
            mDistance = savedInstanceState.getFloat(ARG_KEYS_BUNDLE[3]);
            mNode = savedInstanceState.getParcelable(ARG_KEYS_BUNDLE[4]);
            mLocationMemory = savedInstanceState.getBoolean(ARG_KEYS_BUNDLE[5]);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hunt, container, false);

        // Setup for multiple buttons
        ibAnswer = (ImageButton) view.findViewById(R.id.ibHuntAnswer);
        ibAnswer.setOnClickListener(this);
        ibHint = (ImageButton) view.findViewById(R.id.ibHuntHint);
        ibHint.setOnClickListener(this);
        // Setup view
        iwImage = (ImageView) view.findViewById(R.id.iwHuntImage);
        iwNavigation = (ImageView) view.findViewById(R.id.ivHuntNavigation);
        iwType = (ImageView) view.findViewById(R.id.ivHuntType);
        twQuestion = (TextView) view.findViewById(R.id.tHuntQuestion);
        etAnswer = (EditText) view.findViewById(R.id.etHuntAnswer);
        tvTime = (TextView) view.findViewById(R.id.tvHuntTime);
        setButtons(PLAY_MODE.DEFAULT);

        // FragmentListener
        ((MainActivity) getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                // Show data when the data is refreshed
                showNode();
            }
        });

        // Before return view - Turn off location
        //stLocationUpdates();
        return view;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibHuntAnswer:
                checkAnswer();
                break;
            case R.id.ibHuntHint:
                showHint();
                break;
            default:
                Log.d(TAG, "Reached a none valid button in onClick()");
                Toast.makeText(getActivity(), "This is not a valid button", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onActivityResult(int _reqCode, int _resultCode, Intent _data) {
        super.onActivityResult(_reqCode, _resultCode, _data);

        REQUEST_CODE code = REQUEST_CODE.values()[_reqCode];
        switch(code) {
            case FINISH_CODE:
                if (_resultCode == Activity.RESULT_OK) {
                    // All data has been used <-- reset
                    resetNode();
                    resetPath();
                    showNode();
                    mListener.onStartStopHuntHunt();
                }
                if (_resultCode == Activity.RESULT_CANCELED) {
                    // No data has been saved <-- reset
                    resetNode();
                    resetPath();
                    showNode();
                    mListener.onStartStopHuntHunt();
                }
                break;
            default:
                Log.w(TAG, "Request code is not valid");
                break;
        }
    }

    // This store the listener that will have events fired once the fragment is attached.
    // Data could be passed upwards through these lifecycle Events.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "HuntFragment trigger onStart()");
        if (mLocationMemory) startLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "HuntFragment trigger onResume()");
        //showNode();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "HuntFragment trigger onPause()");

    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "HuntFragment trigger onStop()");
        if (mLocationMemory) stopLocationUpdates();
    }

    // *** Methods called by MainActivity ***
    /** This method update the mPath with the correct Id.
     * @param _pathId
     */
    public void updatePathId(String _pathId) {
        Log.d(TAG, "updatePathId() has been triggered. mPathId will be updated.");
        if(_pathId == NULL_PATH) {
            resetPath();
        }
        else {
            mPathID = _pathId;
            getHuntPath();
            getHuntData();
        }
    }

    /** Method to update userId in HuntFragment called by MainActivity
     */
    public void updateUserId(String _userId) {
        mUserID = _userId;
    }

    /** This method trigger the start of the hunt
     */
    public void updateActiveHunt() {
        Log.d(TAG, "updateStartHunt() has been triggered. Time will start.");
        if (mActiveHunt) {
            startHunt();
        }
        else {
            stopHunt();
        }

    }

    private void startHunt() {
        startTimer();
        startLocationUpdates();
        mNodeNumber = 0;
        mNode = new NodeData(mPath.get(mNodeNumber));
        etAnswer.setEnabled(true);
        Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_timer_24px);
        iwType.setImageDrawable(d);
        iwType.invalidate();
        showNode();
    }

    private void stopHunt() {
        stopTimer();
        stopLocationUpdates();
        etAnswer.setEnabled(false);
        Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_timer_off_24px);
        iwType.setImageDrawable(d);
        iwType.invalidate();
        showNode();
    }


    private void getHuntData() {
        // Firebase connections
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Hunts");
        // Add event listener to path data to recive data from firebase
        ref.child(mPathID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getHuntPath.onDataChanged() has been triggered");
                if (dataSnapshot.exists()) {
                    HuntData tmp = new HuntData(dataSnapshot.getValue(HuntData.class));
                    mTimerCalc.setMs(tmp.getTotaltime());
                } else {
                    Log.d(TAG, "onDataChanged() result into HuntData is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });
    }

    private void getHuntPath() {
        // Firebase connections
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Paths");
        // Add event listener to path data to recive data from firebase
        ref.child(mPathID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getHuntPath.onDataChanged() has been triggered");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        NodeData node = new NodeData(ds.getValue(NodeData.class));
                        mPath.add(node);
                        //mPath.add(new NodeData(ds.getValue(NodeData.class)));
                    }

                } else {
                    Log.d(TAG, "onDataChanged() result into HuntData is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError",
                      "getHuntPath() <-- Fail to read value from database",
                       databaseError.toException());
            }
        });
    }

    /** This method calculate the distance between players new location and Node position.
     *  Result is stored in mDistance which is used in Timehandler updatePosition()
     */
    private void getDistance() {
        mCoordLong = mCurrentLocation.getLongitude();
        mCoordLati = mCurrentLocation.getLatitude();
        if (mPath.size() != 0) {
            double goalCoordLati = mPath.get(mNodeNumber).getCoordLati();
            double goalCoordLong = mPath.get(mNodeNumber).getCoordLong();
            float[] distanceResult = new float[2];
            try {
                Location.distanceBetween(
                        mCoordLati,
                        mCoordLong,
                        goalCoordLati,
                        goalCoordLong,
                        distanceResult);
                mDistance = distanceResult[0];  //result[0]:distance, result[1]:initial bearing, result[2]:final bearing
            }
            catch( IllegalArgumentException e ) {
                Log.d(TAG,"getDistance() <-- Distance calculation failed. Distance is less then 0 or null.");
            }
        }
    }



    // *** All GPS and location methods ***
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                if (mCurrentLocation != null && mPath.size() != 0) {
                    getDistance();
                }
                /*
                if (mCurrentLocation != null ) {
                    Toast.makeText(getActivity(),"Active GPS",Toast.LENGTH_SHORT).show();
                }*/
            }
        };
    }

    private void createLocationRequest() {
        // create location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(SLOW_UPDATE_TIMER);
        mLocationRequest.setFastestInterval(FAST_UPDATE_TIMER);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Set icon to ON
        Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.ic_baseline_location_on_24px);
        iwNavigation.setImageDrawable(d);
        iwNavigation.invalidate();

        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        mFusedLocationClient.requestLocationUpdates(
                                mLocationRequest,
                                mLocationCallback,
                                Looper.myLooper());

                        if (mCurrentLocation != null) {
                            getDistance();
                        }
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                }
                                catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                mRequestLocationUpdate = false;
                                break;
                        }
                        // TODO updateUI

                    }
                });
    }

    private void stopLocationUpdates() {
        // Change icon Off
        Drawable d = ContextCompat.getDrawable(getActivity(),
                R.drawable.ic_baseline_location_off_24px);
        iwNavigation.setImageDrawable(d);
        iwNavigation.invalidate();
        if (!mRequestLocationUpdate) {
            Log.d(TAG, "stopLocationUpdates() <-- GPS will not be used anymore");
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestLocationUpdate = false;

                    }
                });
    }

    // *** ALL METHODS FOR THE GAME ***
    /** This method check the answer from the player if the answer button is pushed. Next node
     *  is loaded if the node is a Quiz or Hunt type it will react accordingly.
     */
    private void checkAnswer() {
        if (mPath.size() != 0 && mActiveHunt) {
            // Get Answer from user make it to lowerCase and remove whitespaces
            String tmpUserAnswer = etAnswer.getText().toString().toLowerCase().replaceAll("\\s+", "");
            String tmpCorrectAnswer = mPath.get(mNodeNumber).getAnswer().toLowerCase();
            if (tmpUserAnswer.equals(tmpCorrectAnswer)) {
                // Answer is correct
                Toast.makeText(getActivity(), "Correct answer", Toast.LENGTH_SHORT).show();
                mNodeNumber++;
                // Check if the player has reached the end of path
                if (checkEndOfGame()) {
                    endGame();
                }
                else {
                    resetNode();
                    mNode = new NodeData(mPath.get(mNodeNumber));
                    // Check which type if game
                    PLAY_MODE mode = PLAY_MODE.values()[mNode.getType()];
                    switch(mode) {
                        case QUIZ: // Quiz - Easy route. User get coordinates and address
                            showAddress();
                            startLocationUpdates();
                            setButtons(PLAY_MODE.DEFAULT); // Disable all buttons
                            break;
                        case HUNT: // Hunt - Normal. User get question and have to find position
                            startLocationUpdates();
                            showNode();
                            break;
                        default:
                            Toast.makeText(getActivity(), "Hunt has none-valid type", Toast.LENGTH_SHORT).show();
                            resetNode();
                            break;
                    }
                }

            } else {
                Toast.makeText(getActivity(), "Wrong answer", Toast.LENGTH_SHORT).show();
                mTimerCalc.removeTime(REMOVE_TIME);
            }
        }
    }

    // User ask for a hint and remove time
    private void showHint() {
        if (mPath.size() != 0) {
            AlertDialog dlg = setHintDialog();
            dlg.show();

        }
    }

    private boolean checkEndOfGame() {
        if (mPath.size() != 0) {
            if (mNodeNumber < mPath.size()) {
                return false;
            } else {
                // Reached end will decrease mNodeNumber so localisation can run safe.
                mNodeNumber--;
                return true;
            }
        }
        return false;
    }

    private void endGame() {
        // Turn off activity
        Toast.makeText(getActivity(), "Congratulation reach the end", Toast.LENGTH_LONG).show();
        stopHunt();
        mActiveHunt = false;
        // Show activity finish and calculate score
        Intent objFinish = new Intent(getActivity(), FinishActivity.class);
        objFinish.putExtra(USER_ID,mUserID);
        objFinish.putExtra(PATH_ID,mPathID);
        objFinish.putExtra(TIME_FINSIHED,mTimerCalc.getMs());
        objFinish.putExtra(TIME_NEG,mTimerCalc.getNeg());
        startActivityForResult(objFinish, REQUEST_CODE.FINISH_CODE.ordinal());
    }

    // New node has been accepted. Reset local data
    private void resetNode() {
        mHintNumber = 0;
        etAnswer.setText(" ");
        //Bitmap e = BitmapFactory.decodeResource(getResources(),R.drawable.walking_gray);
        //iwImage.setImageBitmap(e); // Set default image
        //iwImage.invalidate();
        mNode.clear();
    }

    private void resetPath() {
        mPath.clear();
        mNode.clear();
        mTimerCalc.clear();
        mNodeNumber = 0;
        mHintNumber = 0;
        // Set default picture
        setDefaultNode();
    }

    private void showNode() {
        Log.d(TAG, "showNode() in HuntFragment has been triggered");
        if (mPath.size() != 0 && mNodeNumber < mPath.size()) {
            twQuestion.setText(mNode.getQuestion());
            new DownloadImageFromInternet(iwImage).execute(mNode.getImage());
        }
        else {
            setDefaultNode();
        }
    }

    // Set default node data
    private void setDefaultNode() {
        Bitmap e = BitmapFactory.decodeResource(getResources(),R.drawable.walking_gray);
        iwImage.setImageBitmap(e);
        iwImage.invalidate();
        twQuestion.setText(R.string.tHuntDefaultText);
        tvTime.setText(mTimerCalc.getTimeString());
        tvTime.invalidate();
        //Drawable d = ContextCompat.getDrawable(getActivity(), R.drawable.runer_default);
        //iwImage.setImageDrawable(d); Strange should work
    }

    private void showAddress() {
        Log.d(TAG, "showAddress() in HuntFragment has been triggered");
        if (mPath.size() != 0 && mNodeNumber < mPath.size()) {
            //updatePosition();
            StringBuilder address = new StringBuilder();
            address.append(getResources().getString(R.string.tHuntStreetName) + mNode.getAddress() + "\n");
            address.append(getResources().getString(R.string.tHuntZipNumber) + mNode.getZip() + "\n");
            getDistance();
            address.append(getResources().getString(R.string.tHuntDistance));
            if (mDistance < DISTANCE_TRIGGER) {
                address.append(getResources().getString(R.string.tHuntErrorDistance) + "\n");
            }
            else {
                address.append(mDistance + "\n");
            }
            twQuestion.setText(address);
            Bitmap e = BitmapFactory.decodeResource(getResources(),R.drawable.forest_scale);
            iwImage.setImageBitmap(e);
            setButtons(PLAY_MODE.DEFAULT);
            //Drawable d = ContextCompat.getDrawable(getActivity(),R.drawable.forest_scale);
            //new DownloadImageFromInternet(iwImage).execute("https://www.mstrust.org.uk/sites/default/files/Running_8.svg");
        }
    }

    private String convertCoordToString(double _value) {
        return String.format("%.6f",_value);
    }


    private void startTimer() {
        mTimer = new Timer();
        initTimerTask();
        mTimer.scheduleAtFixedRate(mTimerTaskGPS, 0, SLOW_UPDATE_TIMER); // Schedule the timer to run every 20s
        mTimer.scheduleAtFixedRate(mTimerTaskShowTime, 0, 1000);  // Schedule the timer to run every sec
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
            //Toast.makeText(getActivity(), "Please wait, it may take a few minute...", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bImage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bImage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
            }
            return bImage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    private boolean checkViewForImage(@NonNull ImageView _iv) {
        Drawable drawable = _iv.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }
        return hasImage;
    }

    private void initTimerTask() {
        // Set TimeTask GPS
        mTimerTaskGPS = new TimerTask() {
            @Override
            public void run() {
                mTimeHandlerGPS.post(new Runnable() {
                    @Override
                    public void run() {
                        updatePosition();
                    }
                });
            }
        };
        // Set TimeTask gametimer
        mTimerTaskShowTime = new TimerTask() {
            @Override
            public void run() {
                mTimeHandlerShowTime.post(new Runnable() {
                    @Override
                    public void run() {
                        tvTime.setText(mTimerCalc.getTimeString());
                        mTimerCalc.removeTime(1000);
                        if( mTimerCalc.getMs() <= 0 ) {
                            Toast.makeText(getActivity(), "Negative time", Toast.LENGTH_SHORT).show();
                            mTimerCalc.setNeg(true);
                            // TODO dialog end of game.
                        }
                    }
                });
            }
        };

    }

    private void updatePosition() {
        if (mDistance <= DISTANCE_TRIGGER) {
            PLAY_MODE mode = PLAY_MODE.values()[mNode.getType()];
            switch(mode) {
                case DEFAULT:
                    Log.d(TAG, "Path data has a type data which is not valid");
                    Toast.makeText(getActivity(),
                            "Path data is not correct. Please reload game",
                            Toast.LENGTH_SHORT).show();
                    break;
                case QUIZ: // Quiz - Easy route. User reached coordinates show new Question
                    showNode();
                    setButtons(mode);
                    stopLocationUpdates();
                    break;
                case HUNT: // Hunt - Normal. User get question and have to find position
                    Toast.makeText(getActivity(), "Reached correct location", Toast.LENGTH_SHORT).show();
                    mNodeNumber++;
                    if (checkEndOfGame()) {
                        endGame();
                    }
                    else {
                        resetNode();
                        mNode = new NodeData(mPath.get(mNodeNumber));
                        showNode();
                        switch(mode) {
                            case QUIZ:
                                stopLocationUpdates();
                                setButtons(mode);
                                break;
                            case HUNT:
                                startLocationUpdates();
                                setButtons(mode);
                                break;
                            default:
                                Log.d(TAG, "updatePosition() <-- Node data has none valid type");
                                break;
                        }
                    }
                    break;
                default:
                    Log.w(TAG, "updatePosition() <-- Reached unvalid type");
            }
        }
        else {
            if (DEBUG || SHOW_GPS) {
                Toast.makeText(getActivity(),"Longitude value: " + convertCoordToString(mCoordLong) + "\n" +
                        "Latitude value: " + convertCoordToString(mCoordLati) + "\n" +
                        "Distance: " + mDistance,Toast.LENGTH_LONG).show();
            }

        }
    }

    private void setButtons(PLAY_MODE _type) {
        switch(_type) {
            case DEFAULT:
                ibAnswer.setEnabled(false);
                ibHint.setEnabled(false);
                etAnswer.setEnabled(false);
            case QUIZ:
                ibAnswer.setEnabled(true);
                ibHint.setEnabled(true);
                etAnswer.setEnabled(true);
            break;
            case HUNT:
                ibAnswer.setEnabled(false);
                ibHint.setEnabled(true);
                etAnswer.setEnabled(false);
            break;
        }
    }

    private AlertDialog setHintDialog() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<=mHintNumber && i<mNode.getNoTip(); i++) {
            sb.append(mNode.getTip()[i] + "\n");
        }
        AlertDialog confirmDialogBox = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tHuntHintTitle)
                .setMessage(sb.toString())
                .setIcon(R.drawable.ic_baseline_announcement_24px)
                .setPositiveButton(R.string.default_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mHintNumber++;
                        mTimerCalc.removeTime(REMOVE_TIME);
                        dialog.dismiss();
                    }
                })
                .create();
        return confirmDialogBox;
    }

    // *** All listener declerations and interfaces ***
    public interface OnFragmentInteractionListener {
        void OnHuntInteraction(long _time);
        void onStartStopHuntHunt();
    }

}
