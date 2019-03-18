package com.example.mats.treasurehunt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.mats.treasurehunt.Constants.NULL_PATH;
import static com.example.mats.treasurehunt.Constants.NULL_USER;


public class MainActivity extends AppCompatActivity
        implements HuntFragment.OnFragmentInteractionListener,
                   MainFragment.OnFragmentInteractionListener {

    /**
     * USER = Key for username which is logged in
     * PATH_ID = Key for active/selected path
     * TIME = Key for active time
     * NODE_NUMBER = Key for which index/node the player are at
     */
    private static final String[] BUNDLE_KEYS = {
            "PATH_ID",
            "TIME",
            "NODE_NUMBER",
            "ACTIVE_HUNT",
            "HUNT_PATH",
            "USER_DATA",
            "TOKEN_ID",
            "USER_ID"
    };

    public static final boolean DEBUG = false;                    // Set the debug mode
    protected static long SLOW_UPDATE_TIMER = 20 * 1000;         // 20 s
    protected static long FAST_UPDATE_TIMER = 6 * 1000;          // 6 s
    protected static long REMOVE_TIME = 10 * 1000;               // 10 s
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 9000;
    private static final int REQUEST_SIGN_IN = 9001;
    private final String TAG = "LogMainActivity";

    // Local data in the activity
    private String mPathID = "";
    private String mToken = "";
    // **** Data shared in the Activity ****
    protected static boolean mActiveHunt = false;
    protected static TimeCalc mTimerCalc = new TimeCalc();
    protected static int mNodeNumber = 0;
    //protected static HuntData mHuntData = new HuntData();
    protected static ArrayList<NodeData> mPath = new ArrayList<>();
    protected static UserData mUser = new UserData();
    protected static String mUserID = NULL_USER;

    // Fragments and adapters - UI
    ViewPager viewPager;
    MainViewPagerAdapter adapterViewPager;

    // Listener
    private FragmentRefreshListener fragmentRefreshListener;

    // Firebase auth and google sign-in
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If something was saved in the outState it should be recoverd
        // At first onCreate check if user has Google Play Service
        if (savedInstanceState == null) {
            checkGooglePlayService();
        }
        else {
            // Handle data in MainActivity
            Log.d(TAG, "onCreate() <-- Collected bundle data: ");
            mPathID = savedInstanceState.getString(BUNDLE_KEYS[0]);
            mTimerCalc.setMs(savedInstanceState.getLong(BUNDLE_KEYS[1]));
            mNodeNumber = savedInstanceState.getInt(BUNDLE_KEYS[2]);
            mActiveHunt = savedInstanceState.getBoolean(BUNDLE_KEYS[3]);
            mPath = savedInstanceState.getParcelableArrayList(BUNDLE_KEYS[4]);
            mUser = savedInstanceState.getParcelable(BUNDLE_KEYS[5]);
            mToken = savedInstanceState.getString(BUNDLE_KEYS[6]);
            mUserID = savedInstanceState.getString(BUNDLE_KEYS[7]);
        }

        // Check location (GPS) permission
        checkPermission();
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            checkPermission();
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

        // Setup UI
        viewPager = (ViewPager) findViewById(R.id.vpMainPager);
        adapterViewPager = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);

        // Set Auth listener
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                String tag = makeFragmentTag(viewPager.getId(), 0);   // id: 0=MainFragment, 1=HuntFragment
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if( fragment != null) {
                    if (user != null) {
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                        ((MainFragment) fragment).updateAuth(true);
                    }
                    else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                        ((MainFragment) fragment).updateAuth(false);
                    }
                }

            }
        };

    }


    // *** InstanceState data handling ***
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saved bundle data in MainActivity.");

        // This bundle will be passed to onCreate if the process is killed and restarted.
        outState.putString(BUNDLE_KEYS[0], mPathID);
        outState.putLong(BUNDLE_KEYS[1], mTimerCalc.getMs());
        outState.putInt(BUNDLE_KEYS[2], mNodeNumber);
        outState.putBoolean(BUNDLE_KEYS[3], mActiveHunt);
        outState.putParcelableArrayList(BUNDLE_KEYS[4], mPath);
        outState.putParcelable(BUNDLE_KEYS[5], mUser);
        outState.putString(BUNDLE_KEYS[6], mToken);
        outState.putString(BUNDLE_KEYS[7], mUserID);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "Get bundle data in MainActivity.");

        // Get bundle data check
        mPathID = savedInstanceState.getString(BUNDLE_KEYS[0]);
        mTimerCalc.setMs(savedInstanceState.getLong(BUNDLE_KEYS[1]));
        mNodeNumber = savedInstanceState.getInt(BUNDLE_KEYS[2]);
        mActiveHunt = savedInstanceState.getBoolean(BUNDLE_KEYS[3]);
        mPath = savedInstanceState.getParcelableArrayList(BUNDLE_KEYS[4]);
        mUser = savedInstanceState.getParcelable(BUNDLE_KEYS[5]);
        mToken = savedInstanceState.getString(BUNDLE_KEYS[6]);
        mUserID = savedInstanceState.getString(BUNDLE_KEYS[7]);
    }

    // *** Listeners and events ***
    @Override
    public void onActivityResult( int _reqCode, int _resultCode, Intent _data) {
        super.onActivityResult(_reqCode, _resultCode, _data);
        switch( _reqCode )
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if( _resultCode == Activity.RESULT_OK ) {
                    Intent i = new Intent(this, LocationServices.class);
                    startService(i);
                }
                break;
            case REQUEST_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(_data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                }
                catch (ApiException e) {
                    Log.w(TAG, "onActivityResult() <-- Google sign in failed", e);
                    Toast.makeText(this, "Fail to login on your Google account", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                //super.onActivityResult(_reqCode, _resultCode, _data);
        }
    }



    // *** Lifecycle events ***
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity trigger onStart()");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (mAuthListener != null && currentUser != null){
            if (!currentUser.getUid().equals(mUserID)) {
                FirebaseAuth.getInstance().signOut();
            }
        }
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity trigger onResume()");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity trigger onPause()");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity trigger onStop()");
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // *** Interaction with fragment ***
    @Override
    public void OnHuntInteraction(long _time) {
        long tmp = _time;
    }

    /**
     * This interaction is triggered by the MainFragment and will send the
     * chosen pathId to HuntFragment which will download correct path/game from
     * Firebase. It will later be stored in mPath.
     * @param _pathId [String] pathId to search for in Firebase
     */
    @Override
    public void onUpdateHunt(String _pathId) {
        mPathID = _pathId;
        // Update HuntFragment with new pathId
        String tag = makeFragmentTag(viewPager.getId(), 1);   // id: 0=MainFragment, 1=HuntFragment
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if( fragment != null) {
            ((HuntFragment) fragment).updatePathId(mPathID);
            viewPager.getAdapter().notifyDataSetChanged();
            if( getFragmentRefreshListener() != null) {
                getFragmentRefreshListener().onRefresh();
            }
        }
    }

    /** This method is called by MainFragment. This will update the game according
     *  to what the variable mActiveHunt has been set to.
     */
    @Override
    public void onStartStopHuntMain() {
        String tag = makeFragmentTag(viewPager.getId(), 1);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            ((HuntFragment) fragment).updateActiveHunt();
            if( getFragmentRefreshListener() != null) {
                getFragmentRefreshListener().onRefresh();
            }
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    /** This method is called by HuntFragment. This will update the game according
     *  to what the variable mActiveHunt has been set to.
     */
    @Override
    public void onStartStopHuntHunt() {
        String tag = makeFragmentTag(viewPager.getId(), 0);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            ((MainFragment) fragment).updateActiveHunt();
            //if( getFragmentRefreshListener() != null) {
            //    getFragmentRefreshListener().onRefresh();
            //}
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    /** This method is called when Mainfragment is pushing button "Login".
     * @param _login [boolean] Argument is true if user try to login. False if logging out
     */
    @Override
    public void onLogin(boolean _login) {
        Log.d(TAG,"onLogin() <-- has been triggered. Login:" + String.valueOf(_login));
        if (!DEBUG) {
            if (checkNetworkAvailable(getApplicationContext())) {
                if (_login) {
                    signIn();
                }
                else {
                    signOut();
                }
            }
            else {
                Toast.makeText(this, "Fail to login, due connection failure",Toast.LENGTH_LONG).show();
            }
        }


    }

    /** This method is called when user remove its data. onRevoke will logout and remove auth data
     *  from firebase.
     */
    @Override
    public void onRevoke() {
        if (checkNetworkAvailable(getApplicationContext())) {
            revokeAccess();
        }
        else {
            Toast.makeText(this, "Fail to login, due connection failure",Toast.LENGTH_LONG).show();
        }
    }

    // *** Methods handling login process ***
    private void signIn() {
        Log.d(TAG, "Start the sign in process");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_SIGN_IN);
    }

    private void signOut() {
        Log.d(TAG, "Start the sign out process");
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mUser.clear();
                        mUserID = "";
                        mToken = "";
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mUser.clear();
                        mUserID = NULL_USER;
                        mToken = "";

                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount _account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + _account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(_account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "firebaseAuthWithGoogle()  <-- Success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUser(user);
                        }
                        else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Fail to login, due connection failure",Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }


    // *** Private functions ****
    protected static String makeFragmentTag( int viewId, long id ) {
        return "android:switcher:" + viewId + ":" + id;
    }

    // *** Check for google play service ***
    private void checkGooglePlayService() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(this);
        if( code == ConnectionResult.SUCCESS ) {
            onActivityResult(REQUEST_GOOGLE_PLAY_SERVICES, Activity.RESULT_OK, null);
        }
        else if ( api.isUserResolvableError(code) ) {
                api.getErrorDialog(this, code, REQUEST_GOOGLE_PLAY_SERVICES).show();
        }
        else {
            Toast.makeText(this, api.getErrorString(code), Toast.LENGTH_LONG).show();
        }
    }

    // *** Check that user permit GPS ***
    private void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
           ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                                  Manifest.permission.ACCESS_COARSE_LOCATION},
                                  123);
        }
    }

    // *** Check if the user is connected to internet ***
    private boolean checkNetworkAvailable(Context _context) {
        ConnectivityManager cm = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    // *** Firebase methods ***
    private void checkUser(FirebaseUser _user) {
        Log.d(TAG, "checkUser() <-- User has login get data and check if user exist in Firebase");
        mUserID = _user.getUid();

        // Set values for mUser
        mUser.setEmail(_user.getEmail());     //hmmm
        mUser.setName(_user.getDisplayName());

        if (DEBUG) mUserID = NULL_USER;

        // Connect to Firebase and check user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                Log.d(TAG, "checkUserExist.onDataChanged() has been triggered");
                if( ds.exists() ) {
                    mUser = ds.getValue(UserData.class);
                }
                else {
                    ds.getRef().setValue(mUser);  // If the user does not exist create one
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "Fail to read value from database", databaseError.toException());
            }
        });

        // Update userId in HuntFragment
        String tag = makeFragmentTag(viewPager.getId(), 1); // id: 0=MainFragment, 1=HuntFragment
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if( fragment != null) {
            ((HuntFragment) fragment).updateUserId(mUserID);
        }

    }

    // *** All listener declarations and interfaces ***
    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener _frl) {
        this.fragmentRefreshListener = _frl;
    }

    public interface FragmentRefreshListener {
        void onRefresh();
    }
}
