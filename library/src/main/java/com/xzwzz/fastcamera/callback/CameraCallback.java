package com.xzwzz.fastcamera.callback;

import java.util.List;

/**
 * @author xzwzz
 * @time 2019-11-19
 * @package com.fc62.raisebook.videolive.image.callback
 */
public interface CameraCallback {
    /**
     * 权限允许
     */
    void onSuccess(String url);

    /**
     * 权限拒绝
     */
    void onFailed();
}