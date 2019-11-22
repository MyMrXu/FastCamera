package com.xzwzz.fastcamera;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.xzwzz.fastcamera.callback.CameraCallback;
import com.xzwzz.fastcamera.delegate.CameraDelegateFinder;
import com.xzwzz.fastcamera.delegate.CameraDelegateFragment;


/**
 * @author xzwzz
 * @time 2019-11-19
 * @package com.fc62.raisebook.videolive.image
 */
public class FastCamera {
    private static int requestCode = 0x1132;

    private FastCamera() {

    }


    public static void requestCamera(Context context, String saveUrl, CameraCallback callback) {
        if (context instanceof FragmentActivity) {
            CameraDelegateFragment delegate = findDelegate((FragmentActivity) context);
            if (delegate != null) {
                delegate.requestCamera(context, 0, requestCode++, saveUrl, callback);
            }
        }
    }

    public static void requestZoom(Context context, String saveUrl, CameraCallback callback) {
        if (context instanceof FragmentActivity) {
            CameraDelegateFragment delegate = findDelegate((FragmentActivity) context);
            if (delegate != null) {
                delegate.requestCamera(context, 1, requestCode++, saveUrl, callback);
            }
        }
    }


    /**
     * 构建申请权限用的隐藏的fragment
     */
    private static CameraDelegateFragment findDelegate(FragmentActivity activity) {
        return CameraDelegateFinder.getInstance().find(activity);
    }
}