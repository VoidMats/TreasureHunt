package com.example.mats.treasurehunt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    private boolean hasNetwork;
    private boolean checked;

    public NetworkUtil() {
        this.hasNetwork = false;
        this.checked = false;
    }

    // *** Check if the user is connected to internet ***
    // TODO do this as a Task
    public boolean checkNetworkAvailable(Context _context) {
        ConnectivityManager cm = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        hasNetwork = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        checked = true;
        return hasNetwork;
    }

    public boolean checkNetWorkAvailable() {
        if (checked)
            return hasNetwork;
        else
            return false;
    }

    public String errorMessage() {
        StringBuilder sb = new StringBuilder("No network data application can't receive or saved data");
        return sb.toString();
    }
}
