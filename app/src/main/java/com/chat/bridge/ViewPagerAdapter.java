package com.chat.bridge;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by SHAAN on 14-07-17.
 */

class ViewPagerAdapter extends FragmentPagerAdapter{
    private static final int NO_OF_TABS = 3;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return RequestsFragment.newInstance();
            case 1:
                return  ChatsFragment.newInstance();
            case 2:
                return FriendsFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NO_OF_TABS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Requests";
            case 1:
                return  "Chats";
            case 2:
                return "Friends";
            default:
                return null;
        }
    }
}
