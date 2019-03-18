package com.example.mats.treasurehunt;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;

import com.example.mats.treasurehunt.HuntFragment;
import com.example.mats.treasurehunt.MainFragment;

import java.util.ArrayList;
import java.util.List;

// PageAdapter for the fragments
public class PagerAdapter extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 2;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private ArrayList<String> mFragmentTitle = new ArrayList<>();

    public PagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem( int _position) {
        return mFragmentList.get( _position );
    }

    /*
    @Override
    public Fragment getItem( int _position )
    {
        switch (_position) {
            case 0:
                return MainFragment.newInstance( "NotSelected" );
            case 1:
                return HuntFragment.newInstance( "NotSelected", 2, 0);
            default:
                return null;
        }
    } */

    public void addFragment( Fragment _fragment, String _title) {
        mFragmentList.add( _fragment );
        mFragmentTitle.add( _title );
    }

    /*
    public Fragment getItemByTag( String _tag ) {
        return this.getItemByTag( _tag );
    }*/

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle( int _position ) {
        return mFragmentTitle.get( _position );
    }

    /*
    public static class ArrayListFragment extends ListFragment {

        int mNum;

        static ArrayListFragment newInstance( int _num ) {
            ArrayListFragment f = new ArrayListFragment();

            // Supply num input as an argument
            Bundle args = new Bundle();
            args.putInt("num", _num);
            f.setArguments(args);
            return f;
        }
    } */

}