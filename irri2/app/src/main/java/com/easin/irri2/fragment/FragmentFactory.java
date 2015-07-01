package com.easin.irri2.fragment;

import android.app.Fragment;

import com.easin.irri2.ActMain;
import com.easin.irri2.R;


/**
 * Created by admin on 13-11-23.
 */
public class FragmentFactory {
    public static Fragment getInstanceByIndex(int index,ActMain _actmain) {
        Fragment fragment = null;
        switch (index) {
            case R.id.rb_realtime:
                fragment = com.easin.irri2.RealTimeFragment.newInstance("hello","abc",_actmain);
                break;
            case R.id.rb_histroy:
                fragment = com.easin.irri2.HistoryFragment.newInstance("hello","abc",_actmain);
                break;
            case R.id.rb_mylist:
                fragment = com.easin.irri2.MyFragment.newInstance("hello","abc",_actmain);
                break;
            case R.id.rb_global:
                fragment = new GlobalFragment();
                break;
        }
        return fragment;
    }
}
