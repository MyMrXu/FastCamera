package com.xzwzz.fastcamera;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author xzwzz
 * @time 2020-05-02
 * @package com.xzwzz.fastcamera.util
 */
class CameraUtils {

    public static Intent createCamreaIntent(Uri uri, Context context) throws Exception {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(context.getPackageManager()) != null) {
            if (uri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                captureIntent.putExtra("return-data", true);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            return captureIntent;
        }
        return null;
    }

    public static Intent createZoomIntent(Uri uri, Uri outputUri, Context context) throws Exception {
        Log.d("xzwzz", "createZoomIntent: " + uri + "\n" + uri);
        //设置Intent
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("xzwzz_url", uri);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 512);
        intent.putExtra("outputY", 512);
        intent.putExtra("return-data", true);
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
//        intent.putExtra("noFaceDetection", false);//去除默认的人脸识别，否则和剪裁匡重叠
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 图片格式
//        startActivityForResult(intent, request_code);//这里就将裁剪后的图片的Uri返回了
        return intent;
    }

    public static Uri buildOutputUri(Context context) {
        Uri photoUri = null;
//        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            photoUri = createImageUri(context);
//        } else {
        File photoFile = null;
        try {
            photoFile = createImageFile(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = CameraUriUtils.file2Uri(context, photoFile);
            } else {
                photoUri = Uri.fromFile(photoFile);
            }
        }
//        }
        return photoUri;
    }

    public static Uri buildZoomOutputUri(Context context) {
        File photoFile = null;
        try {
            photoFile = createImageFile(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(photoFile);
    }

    private static Uri createImageUri(Context context) {
        String status = Environment.getExternalStorageState();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.DESCRIPTION, context.getPackageName() + "\r" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
        }
    }

    private static File createImageFile(Context context) throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName + ".jpg");
        return tempFile;
    }

}
