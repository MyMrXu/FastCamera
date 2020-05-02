package com.xzwzz.fastcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.xzwzz.fastcamera.callback.CameraCallback;

/**
 * @author xzwzz
 * @time 2019-11-19
 * @package com.fc62.raisebook.videolive.image.delegate
 */
public final class CameraDelegateFragment extends Fragment implements LifecycleObserver {
    private SparseArrayCompat<RequestEntry> requestArray = new SparseArrayCompat<RequestEntry>();
    private LifecycleObserver mObserver;
    private static final int CAMERA = 0, ZOOM = 1;

    public static CameraDelegateFragment newInstance() {
        return new CameraDelegateFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        popAll();
        getLifecycle().removeObserver(mObserver);
    }


    /**
     * 请求操作必须在OnAttach后调用
     *
     * @param entry 请求包装对象
     */
    private void pushStack(final RequestEntry entry) {
        requestArray.put(entry.hashCode(), entry);
        mObserver = new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            public void attched() {
                requestArray.get(entry.hashCode()).getRunnable().run();
                getLifecycle().removeObserver(this);
            }
        };
        this.getLifecycle().addObserver(mObserver);
    }

    /**
     * 结束任务，在集合中移除
     *
     * @param entry 要移除的请求包装对象
     */
    private void popStack(RequestEntry entry) {
        requestArray.remove(entry.hashCode());
    }

    /**
     * 移除所有callback
     */
    private void popAll() {
        if (requestArray != null && requestArray.size() > 0) {
            requestArray.clear();
        }
    }

    private Uri mCameraUri;

    public void requestCamera(final Context context, final int type, final int request_code, final Uri uri, final CameraCallback callback) {
        mCameraUri = CameraUtils.buildOutputUri(context);
        pushStack(RequestEntry.newBuilder().withCallback(callback).withRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    if (type == CAMERA) {
                        startActivityForResult(CameraUtils.createCamreaIntent(mCameraUri, context), request_code);
                    } else if (type == ZOOM) {
                        startActivityForResult(CameraUtils.createZoomIntent(uri, mCameraUri, context), request_code);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailed();
                }
            }
        }).withRequestCode(request_code).build());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (int i = 0; i < requestArray.size(); i++) {
            RequestEntry entry = requestArray.valueAt(i);
            if (requestCode == entry.getRequestCode()) {
                if (resultCode == Activity.RESULT_OK) {
                    entry.getCallback().onSuccess(mCameraUri);
                } else {
                    entry.getCallback().onFailed();
                }
                popStack(entry);
                break;
            }
        }
    }
}
