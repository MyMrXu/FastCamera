package com.xzwzz.fastcamera.delegate;


import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * @author xzwzz
 * @time 2019-11-19
 * @package com.fc62.raisebook.videolive.image.delegate
 */
public class CameraDelegateFinder {
    private static final String DELEGATE_FRAGMENT_TAG = CameraDelegateFragment.class.getSimpleName() + "Tag";

    private static class Singleton {
        private static final CameraDelegateFinder instance = new CameraDelegateFinder();
    }

    public static CameraDelegateFinder getInstance() {
        return Singleton.instance;
    }

    /**
     * 添加隐藏权限fragment
     */
    public CameraDelegateFragment find(FragmentActivity activity) {
        CameraDelegateFragment fragment = null;
        if (activity != null && !activity.isFinishing()) {
            FragmentManager fm = activity.getSupportFragmentManager();
            fragment = (CameraDelegateFragment) fm.findFragmentByTag(DELEGATE_FRAGMENT_TAG);
            if (fragment == null) {
                fragment = CameraDelegateFragment.newInstance();
                fm.beginTransaction()
                        .add(fragment, DELEGATE_FRAGMENT_TAG)
                        .commitAllowingStateLoss();
            }
        }
        return fragment;
    }
}
