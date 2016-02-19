package com.example.denniskim.ribbit2.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.denniskim.ribbit2.R;
import com.example.denniskim.ribbit2.UI.FriendsFragment;
import com.example.denniskim.ribbit2.UI.InboxFragment;

import java.util.Locale;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 *
 * THIS WILL DISPLAY THE LIST OF FRIENDS IN THE FRIENDS FRAGMENT
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    protected Context mContext;
    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        switch(position)
        {
            case 0:
                return new InboxFragment();
            case 1:
                return new FriendsFragment();
        }
        return null;
    }

    // returns the amount of fragment tabs that we want
    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    // sets the title of the fragment tabs
    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return mContext.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return mContext.getString(R.string.title_section2).toUpperCase(l);
        }
        return null;
    }

    public int getIcon(int position)
    {
        switch (position) {
            case 0:
                return R.drawable.ic_tab_inbox;
            case 1:
                return R.drawable.ic_tab_friends;
        }
        return R.drawable.ic_tab_inbox;
    }
}