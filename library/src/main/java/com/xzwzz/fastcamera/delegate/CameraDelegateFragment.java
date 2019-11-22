package com.xzwzz.fastcamera.delegate;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.util.SparseArrayCompat;


import com.xzwzz.fastcamera.BuildConfig;
import com.xzwzz.fastcamera.callback.CameraCallback;
import com.xzwzz.fastcamera.entity.RequestEntry;
import com.xzwzz.fastcamera.util.PathUtils;

import java.io.File;

/**
 * @author xzwzz
 * @time 2019-11-19
 * @package com.fc62.raisebook.videolive.image.delegate
 */
public class CameraDelegateFragment extends Fragment implements LifecycleObserver {
    //权限回调的标识
    private static final int REQUEST_CODE = 0X0122;
    private SparseArrayCompat<RequestEntry> requestArray = new SparseArrayCompat<RequestEntry>();
    private LifecycleObserver mObserver;
    private static final int CAMERA = 0, ZOOM = 1;
    private String saveUrl;

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

    public void requestCamera(Context context, final int type, final int request_code, final String saveUrl, final CameraCallback callback) {
        pushStack(RequestEntry.newBuilder().withCallback(callback).withRunnable(new Runnable() {
            @Override
            public void run() {
                if (type == CAMERA) {
                    takePhoto(saveUrl, request_code);
                } else if (type == ZOOM) {
                    startPhotoZoom(saveUrl, request_code);
                }
            }
        }).withRequestCode(request_code).build());
    }

    private void takePhoto(String photoUrl, int request_code) {
        saveUrl = photoUrl;
        File file = new File(photoUrl);//拍照文件的路径
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(getContext(), getActivity().getApplication().getPackageName() + ".fileprovider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            Uri uri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        startActivityForResult(intent, request_code);
    }

    public void startPhotoZoom(String inputUrl, int request_code) {
        //输出存储路径
        String[] strings = inputUrl.split("\\.");
        String cropCapturepath = strings[0] + "_crop." + strings[1];
        saveUrl = cropCapturepath;
        //设置Intent
        Intent intent = new Intent("com.android.camera.action.CROP");
        //sdk>=24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri inputUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplication().getPackageName() + ".fileprovider", new File(inputUrl));
            Uri outPutUri = Uri.fromFile(new File(cropCapturepath));
            intent.setDataAndType(inputUri, "image/*");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            Uri inputUri = Uri.fromFile(new File(inputUrl));
            Uri outPutUri = Uri.fromFile(new File(cropCapturepath));
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                String url = PathUtils.getPath(getActivity(), inputUri);//这个方法是处理4.4以上图片返回的Uri对象不同的处理方法
                intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
            } else {
                intent.setDataAndType(inputUri, "image/*");
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
        }


        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 400);
        intent.putExtra("outputY", 400);
        intent.putExtra("return-data", false);
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        intent.putExtra("noFaceDetection", false);//去除默认的人脸识别，否则和剪裁匡重叠
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
                    String resultUrl = saveUrl;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && data.getData() != null) {
                        resultUrl = PathUtils.getPath(getActivity(), data.getData());
                    }
                    entry.getCallback().onSuccess(resultUrl);
                } else {
                    entry.getCallback().onFailed();
                }
                popStack(entry);
                break;
            }
        }

    }
}
