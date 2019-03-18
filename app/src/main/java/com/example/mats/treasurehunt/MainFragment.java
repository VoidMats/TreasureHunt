package com.example.mats.treasurehunt;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.mats.treasurehunt.Constants.NULL_USER;
import static com.example.mats.treasurehunt.Constants.PATH_ID;
import static com.example.mats.treasurehunt.Constants.SETTING_RESULT_NAME;
import static com.example.mats.treasurehunt.Constants.SETTING_RESULT_REVOKE;
import static com.example.mats.treasurehunt.Constants.USER_ID;
import static com.example.mats.treasurehunt.Constants.USER;
import static com.example.mats.treasurehunt.Constants.NULL_PATH;
import static com.example.mats.treasurehunt.MainActivity.DEBUG;
import static com.example.mats.treasurehunt.MainActivity.mTimerCalc;
import static com.example.mats.treasurehunt.MainActivity.mActiveHunt;
import static com.example.mats.treasurehunt.MainActivity.mUser;
import static com.example.mats.treasurehunt.MainActivity.mUserID;


public class MainFragment extends Fragment implements View.OnClickListener {

    // the fragment static const variables
    private final String TAG = "LogMainFragment";
    private enum REQUEST_CODE {
            SEARCH_CODE,
            SETTINGS_CODE,
            PROFILE_CODE
    };
    private static final String[] BUNDLE_KEYS = {
            "PATH_ID",
            "PATH_DATA"
    };

    // Local variables
    private String mPathID = "";
    private HuntData mHuntData = new HuntData();
    //private TimeCalc mLocalTimer = new TimeCalc();

    // UI variables
    private Button bSearch;
    private Button bSettings;
    private Button bProfile;
    private Button bLogin;
    private Button bStart;
    private Button bStop;
    private Button bPause;
    private TextView tvName;
    private TextView tvId;
    private TextView tvTime;
    private TextView tvNoNodes;
    private TextView tvType;
    private TextView tvLocation;

    // Listener
    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * @param _pathID Id of the Hunt path which will be used to collect data from Firebase.
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance(String _pathID) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(PATH_ID, _pathID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Collect arguments when the fragment is called for the first time
        if (getArguments() != null) {
            mPathID = getArguments().getString(PATH_ID);
        }
        // Collect bundle data on changed instance
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Collect bundle data if any
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Setup UI
        bSearch = (Button) view.findViewById(R.id.bMainSearch);
        bSearch.setOnClickListener(this);
        bSettings = (Button) view.findViewById(R.id.bMainSettings);
        bSettings.setOnClickListener(this);
        bProfile = (Button) view.findViewById(R.id.bMainProfile);
        bProfile.setOnClickListener(this);
        bLogin = (Button) view.findViewById(R.id.bMainLogin);
        bLogin.setOnClickListener(this);
        bStart = (Button) view.findViewById(R.id.bMainStart);
        bStart.setOnClickListener(this);
        bStop = (Button) view.findViewById(R.id.bMainStop);
        bStop.setOnClickListener(this);
        bPause = (Button) view.findViewById(R.id.bMainPause);
        bPause.setOnClickListener(this);
        tvName = view.findViewById(R.id.tMainNameScr);
        tvId = view.findViewById(R.id.tMainIdScr);
        tvTime = view.findViewById(R.id.tMainTimeScr);
        tvNoNodes = view.findViewById(R.id.tMainNoPostionsScr);
        tvType = view.findViewById(R.id.tMainTypeScr);
        tvLocation = view.findViewById(R.id.tMainLocationScr);

        // Place local variables into UI
        showHuntData();
        disableButtonsActiveGame();
        if (DEBUG)
            disableButtonAuth(false);
        else
            disableButtonAuth(true);

        return view;
    }

    // *** InstanceState data handling ***
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEYS[0], mPathID);
        outState.putParcelable(BUNDLE_KEYS[1], mHuntData);
    }

    private void onRestoreInstanceState(Bundle savedInstanceState) {
        mPathID = savedInstanceState.getString(BUNDLE_KEYS[0]);
        mHuntData = savedInstanceState.getParcelable(BUNDLE_KEYS[1]);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bMainSearch:
                Intent objSearch = new Intent(getActivity(), SearchActivity.class);
                objSearch.putExtra(USER_ID, mUserID);
                startActivityForResult(objSearch, REQUEST_CODE.SEARCH_CODE.ordinal());
                break;
            case R.id.bMainSettings:
                if (!mUserID.equals(NULL_USER) || DEBUG) {
                    Intent objSetting = new Intent(getActivity(), SettingsActivity.class);
                    objSetting.putExtra(USER_ID, mUserID);
                    startActivityForResult(objSetting, REQUEST_CODE.SETTINGS_CODE.ordinal());
                }
                break;
            case R.id.bMainProfile:
                if (!mUserID.equals(NULL_USER) || DEBUG) {
                    Intent objProfile = new Intent(getActivity(), ProfileActivity.class);
                    objProfile.putExtra(USER_ID, mUserID);
                    startActivityForResult(objProfile,
                            REQUEST_CODE.PROFILE_CODE.ordinal(),
                            ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                }
                break;
            case R.id.bMainLogin:
                String btnText = bLogin.getText().toString();
                String loginText = getResources().getString(R.string.bMainLogin);
                if ( btnText.equals(loginText) || DEBUG ) { // If the button has text Login --> go login
                    mListener.onLogin(true);
                }
                else { // --> Logout
                    mListener.onLogin(false);
                }
                break;
            case R.id.bMainStart:
                if (mHuntData.isEmpty()) {
                    Toast.makeText(getActivity(),"No hunt has been selected. Please search for a hunt", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), "Hunt has started", Toast.LENGTH_SHORT).show();
                    mActiveHunt = true;
                    disableButtonsActiveGame();
                    mListener.onStartStopHuntMain();
                }
                break;
            case R.id.bMainStop:
                // This shouldn't happened
                if (mHuntData.isEmpty() ) {
                    Toast.makeText(getActivity(),"No hunt has been selected. Please search for a hunt", Toast.LENGTH_SHORT).show();
                }
                // Create an AlertDialog that hunt will stop and be removed. Execution of code in the dialog
                else {
                    Toast.makeText(getActivity(), "Stop hunt", Toast.LENGTH_SHORT).show();
                    AlertDialog dlg = setConfirmDialog();
                    dlg.show();
                }
                break;
            case R.id.bMainPause:
                if (mHuntData.isEmpty()) {
                    Toast.makeText(getActivity(),"No hunt has been selected. Please search for a hunt", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), "Pause hunt", Toast.LENGTH_SHORT).show(); //TODO language
                    mActiveHunt = false;
                    disableButtonsActiveGame();
                    mListener.onStartStopHuntMain();
                }
                break;
            default:
                Toast.makeText(getActivity(), "This is not a valid button", Toast.LENGTH_SHORT).show();
        }
    }

    // If there is a result from an Activity
    @Override
    public void onActivityResult(int _reqCode, int _resultCode, Intent _data) {
        super.onActivityResult(_reqCode, _resultCode, _data);

        REQUEST_CODE code = REQUEST_CODE.values()[_reqCode];
        switch(code) {
            case SEARCH_CODE: // User has select a hunt. Passing pathId to MainActivity
                if(_resultCode == Activity.RESULT_OK) {
                    mPathID = _data.getStringExtra("PATH_ID");
                    Toast.makeText(getActivity(), mPathID, Toast.LENGTH_SHORT).show();
                    getHuntData();
                    mListener.onUpdateHunt(mPathID);
                }
                if(_resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getActivity(), "Activity has no data", Toast.LENGTH_SHORT).show();
                }
                break;
            case SETTINGS_CODE: // User has changed settings
                if(_resultCode == Activity.RESULT_OK) {
                    boolean revoke = _data.getBooleanExtra(SETTING_RESULT_REVOKE, false);
                    StringBuilder msg = new StringBuilder(_data.getStringExtra(SETTING_RESULT_NAME));
                    if (!revoke) {
                        msg.insert(0, "User data saved: ");
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mListener.onRevoke();
                        msg.insert(0, "User is logged out and can be switched: ");
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

                    }
                }
                if(_resultCode == Activity.RESULT_CANCELED) { // Is called when backward is pressed
                    Toast.makeText(getActivity(), "USER data was not saved", Toast.LENGTH_SHORT).show();
                }
                break;
            case PROFILE_CODE: // User can check its profile
                if(_resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "onActivityResult() <-- Profile pushed button");
                }
                if(_resultCode == Activity.RESULT_CANCELED) {
                    Log.d(TAG, "onActivityResult() <-- Profile pushed backward or unexpected error");
                }
                break;
            default:
                Log.w(TAG, "Request code is not valid.");
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
        Log.d(TAG, "MainFragment trigger onStart()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "MainFragment trigger onStop();");
    }

    // *** Methods called by MainActivity ***
    public void updateAuth(boolean _validAuth) {
        if (_validAuth) {
            bLogin.setText(getResources().getString(R.string.bMainLogout));
            disableButtonAuth(false);
            if (DEBUG) Toast.makeText(getActivity(),"Login",Toast.LENGTH_SHORT).show();
        }
        else {
            bLogin.setText(R.string.bMainLogin);
            disableButtonAuth(true);
            if (DEBUG) Toast.makeText(getActivity(), "Logout",Toast.LENGTH_SHORT).show();
        }
    }

    /** This method called by MainActivity
     */
    public void updateActiveHunt() {
        Log.d(TAG, "updateStartHunt() has been triggered. Time will start.");
        if (!mActiveHunt) {
            //clear data and buttons
            mPathID = NULL_PATH;
            mHuntData.clear();
        }
        showHuntData();
        disableButtonsActiveGame();
    }

    private AlertDialog setConfirmDialog() {
        AlertDialog confirmDialogBox = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tHuntAlertStopTitle)
                .setMessage(R.string.tHuntAlertStopMessage)
                .setIcon(R.drawable.ic_baseline_block_24px)
                .setPositiveButton(R.string.tConfirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Stop Hunt
                        mActiveHunt = false;
                        disableButtonsActiveGame();
                        mListener.onStartStopHuntMain();
                        // Update with a NULL_PATH <-- delete path
                        mListener.onUpdateHunt(NULL_PATH);
                        // Refresh view
                        mListener.onStartStopHuntMain();
                        // Delete data in MainFragment
                        mHuntData.clear();
                        showHuntData();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.tContinue, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return confirmDialogBox;
    }



    private void getHuntData() {
        // Firebase connections
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Hunts");
        // Add event listener to path data to recive data from firebase
        ref.child(mPathID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getHuntData().onDataChanged() has been triggered");
                if( dataSnapshot.exists() ) {
                    HuntData data = dataSnapshot.getValue(HuntData.class);
                    mHuntData = data;
                    mTimerCalc.setMs(mHuntData.getTotaltime());
                    showHuntData();
                }
                else {
                    Log.d(TAG, "onDataChanged() result into HuntData is null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });
    }

    private void showHuntData() {
        Constants.PLAY_MODE mode = Constants.PLAY_MODE.values()[mHuntData.getType()];
        tvName.setText(mHuntData.getName());
        tvId.setText(mHuntData.getId());
        tvTime.setText(mTimerCalc.getTimeString(mHuntData.getTotaltime()));
        tvNoNodes.setText(String.valueOf(mHuntData.getNoNodes()));
        tvType.setText(mode.toString());
        tvLocation.setText(mHuntData.getLocation());
    }

    private void disableButtonsActiveGame() {
        // If mActiveHunt is true <-- Ongoing game disable start,search and enable stop and pause
        bSearch.setEnabled(!mActiveHunt);
        bStart.setEnabled(!mActiveHunt);
        bStop.setEnabled(mActiveHunt);
        bPause.setEnabled(mActiveHunt);
    }

    private void disableButtonAuth(boolean _true) {
        bSettings.setEnabled(!_true);
        bProfile.setEnabled(!_true);
    }

    public interface OnFragmentInteractionListener {
        void onUpdateHunt(String _pathId);
        void onStartStopHuntMain();
        void onLogin(boolean _login);
        void onRevoke();
    }
}
