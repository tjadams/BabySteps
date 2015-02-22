package com.lorentzos.swipecards;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by arkadyark on 15-02-21.
 */
public class DashboardTabAdapter extends FragmentPagerAdapter {

    private String username;

    public DashboardTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return DashboardListFragment.newInstance(position, username);
    }

    @Override
    public int getCount() {
        return 3;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Comfort Zone";
        } else if (position == 1) {
            return "Learning Zone";
        } else {
            return "Danger Zone";
        }
    }
}
