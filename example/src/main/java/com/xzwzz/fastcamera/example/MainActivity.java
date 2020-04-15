package com.xzwzz.fastcamera.example;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.xzwzz.fastcamera.FastCamera;
import com.xzwzz.fastcamera.callback.CameraCallback;
import com.xzwzz.fastcamera.util.UriUtils;


/**
 * @author xzwzz
 * @time 2019-11-22
 * @package com.xzwzz.fastcamera
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnGetPhoto;
    private Button mBtnGetZoom;
    private ImageView resuleImageView;
    private Uri uri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBtnGetPhoto = (Button) findViewById(R.id.btn_get_photo);
        resuleImageView = (ImageView) findViewById(R.id.iv_photo);
        mBtnGetZoom = (Button) findViewById(R.id.btn_get_zoom);

        mBtnGetPhoto.setOnClickListener(this);
        mBtnGetZoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_photo:
                FastCamera.requestCamera(this, new CameraCallback() {
                    @Override
                    public void onSuccess(Uri u) {
                        uri = u;
                        resuleImageView.setImageURI(u);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
                break;
            case R.id.btn_get_zoom:
                if (uri == null) {
                    return;
                }
                FastCamera.requestZoom(this, uri, new CameraCallback() {
                    @Override
                    public void onSuccess(Uri url) {
                        resuleImageView.setImageURI(url);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
                break;
        }
    }
}
