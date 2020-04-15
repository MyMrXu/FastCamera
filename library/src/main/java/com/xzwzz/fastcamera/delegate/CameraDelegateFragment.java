package com.xzwzz.fastcamera.delegate;

import android.app.Activity;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.fragment.app.Fragment;
import androidx.core.os.EnvironmentCompat;
import androidx.collection.SparseArrayCompat;

import android.util.Log;


import com.xzwzz.fastcamera.callback.CameraCallback;
import com.xzwzz.fastcamera.entity.RequestEntry;
import com.xzwzz.fastcamera.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author xzwzz
 * @time 2019-11-19
 * @package com.fc62.raisebook.videolive.image.delegate
 */
public class CameraDelegateFragment extends Fragment implements LifecycleObserver {
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

    //用于保存拍照图片的uri
    private Uri mCameraUri;

    private void openCamera(int requestCode) {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            mCameraUri = buildOutputUri();
            Log.e("xzwzz", "buildOutputUri: " + mCameraUri.getPath());
            if (mCameraUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
                captureIntent.putExtra("return-data", true);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, requestCode);
            }
        }
    }

    private Uri buildOutputUri() {
        Uri photoUri = null;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            photoUri = createImageUri();
        } else {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("xzwzz", "buildOutputUri: " + photoFile.getAbsolutePath());
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                    photoUri = UriUtils.file2Uri(getActivity(), photoFile);
                } else {
                    photoUri = Uri.fromFile(photoFile);
                }
            }
        }
        return photoUri;
    }

    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.DESCRIPTION, getActivity().getPackageName() + "\r" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return getActivity().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    public void requestCamera(Context context, final int type, final int request_code, final Uri uri, final CameraCallback callback) {
        pushStack(RequestEntry.newBuilder().withCallback(callback).withRunnable(new Runnable() {
            @Override
            public void run() {
                if (type == CAMERA) {
                    openCamera(request_code);
                } else if (type == ZOOM) {
                    startPhotoZoom(uri, request_code);
                }
            }
        }).withRequestCode(request_code).build());
    }

    public void startPhotoZoom(Uri uri, int request_code) {
        //设置Intent
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        mCameraUri = createImageUri();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);

        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 512);
        intent.putExtra("outputY", 512);
        intent.putExtra("return-data", true);
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
//        intent.putExtra("noFaceDetection", false);//去除默认的人脸识别，否则和剪裁匡重叠
        intent.putExtra("outputFormat", "JPEG");
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 图片格式
        startActivityForResult(intent, request_code);//这里就将裁剪后的图片的Uri返回了
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
