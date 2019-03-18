package com.example.mats.treasurehunt;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.example.mats.treasurehunt.Constants.NULL_PATH;

public class PagerAdapter extends FragmentPagerAdapter{

    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    //private ArrayList<String> registeredTitle = new ArrayList<String>();
    private Context context;

    // Constructor
    public PagerAdapter(Context c, FragmentManager fm) {
        super(fm);

        this.context = c;
    }

    @Override
    public int getCount() {
        return registeredFragments.size();
    }

    @Override
    public Fragment getItem( int _position )
    {
        switch (_position) {
            case 0:
                return MainFragment.newInstance( "NotSelected" );
            case 1:
                return HuntFragment.newInstance( "NotSelected", NULL_PATH);
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup _container, int _postition ) {
        Fragment fragment = (Fragment) super.instantiateItem(_container, _postition);
        registeredFragments.put(_postition, fragment);
        return fragment;
    }

    @Override
    public void destroyItem( ViewGroup _container, int _position, Object _object) {
        registeredFragments.remove( _position );
        super.destroyItem( _container, _position, _object );

    }

    public Fragment getRegisteredFragment( int _position ) {
        return registeredFragments.get( _position );
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle( int _position ) {
        return String.valueOf( _position );
    }

}
