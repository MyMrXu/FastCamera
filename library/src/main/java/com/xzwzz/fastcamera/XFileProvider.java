package com.xzwzz.fastcamera;

import android.app.Application;

import androidx.core.content.FileProvider;

public class XFileProvider extends FileProvider {
    @Override
    public boolean onCreate() {
        return true;
    }
}
