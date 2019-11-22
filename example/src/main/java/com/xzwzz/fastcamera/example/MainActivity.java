package com.xzwzz.fastcamera.example;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xzwzz.fastcamera.FastCamera;
import com.xzwzz.fastcamera.callback.CameraCallback;


/**
 * @author xzwzz
 * @time 2019-11-22
 * @package com.xzwzz.fastcamera
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnGetPhoto;
    private Button mBtnGetZoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBtnGetPhoto = (Button) findViewById(R.id.btn_get_photo);
        mBtnGetZoom = (Button) findViewById(R.id.btn_get_zoom);

        mBtnGetPhoto.setOnClickListener(this);
        mBtnGetZoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_photo:
                FastCamera.requestCamera(this, Environment.getExternalStorageDirectory() + "/image.jpg", new CameraCallback() {
                    @Override
                    public void onSuccess(String url) {
                        Log.e("xzwzz", "onSuccess: " + url);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
                break;
            case R.id.btn_get_zoom:
                FastCamera.requestZoom(this, Environment.getExternalStorageDirectory() + "/image.jpg", new CameraCallback() {
                    @Override
                    public void onSuccess(String url) {
                        Log.e("xzwzz", "onSuccess: " + url);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
                break;
        }
    }
}
