package com.example.mats.treasurehunt;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import static com.example.mats.treasurehunt.Constants.NULL_PATH;
import static com.example.mats.treasurehunt.Constants.NULL_USER;

public class MainViewPagerAdapter extends FragmentPagerAdapter{

    private static int NUM_ITEMS = 2;

    // Constructor
    public MainViewPagerAdapter( FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem( int _position )
    {
        switch (_position) {
            case 0:
                return MainFragment.newInstance( NULL_USER );
            case 1:
                return HuntFragment.newInstance( NULL_USER, NULL_PATH);
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle( int _position ) {
        return String.valueOf( _position );
    }
}
