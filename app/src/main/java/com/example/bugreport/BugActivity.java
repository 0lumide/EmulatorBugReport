package com.example.bugreport;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.List;

import static android.app.WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER;

public class BugActivity extends AppCompatActivity {
    final String TAG = "com.example.bugreport";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bugreport);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWallpaper();
            }
        });
    }

    private void setWallpaper() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PICTURE);
    }

    final int PICK_PICTURE = 11;

    @Override
    @TargetApi(19)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PICTURE) {
            if (resultCode == RESULT_OK) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

                debug(data.getData());

                // Throws IllegalArgumentException: "Cannot use passed URI to set wallpaper;
                // check that the type returned by ContentProvider matches image/*"
                Intent intent = wallpaperManager.getCropAndSetWallpaperIntent(data.getData());
                startActivityForResult(intent, 0);
            }
        }
    }

    /**
     * Modifed from:
     * https://android.googlesource.com/platform/frameworks/base.git/+/5527443/core/java/android/app/WallpaperManager.java#872
     */
    @TargetApi(19)
    void debug(Uri uri) {
        Log.d(TAG, "uri: " + uri.toString());
        final PackageManager packageManager = getPackageManager();
        Intent cropAndSetWallpaperIntent =
                new Intent(ACTION_CROP_AND_SET_WALLPAPER, uri);
        cropAndSetWallpaperIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // fallback crop activity
        // final String cropperPackage = mContext.getString(com.android.internal.R.string.config_wallpaperCropperPackage);
        // No direct access to internal resources hence the work around
        int intId = Resources.getSystem().getIdentifier("config_wallpaperCropperPackage", "string", "android");
        final String cropperPackage = getString(intId);
        Log.d(TAG, "cropperPackage: " + cropperPackage);

        cropAndSetWallpaperIntent.setPackage(cropperPackage);
        List<ResolveInfo> cropAppList = packageManager.queryIntentActivities(
                cropAndSetWallpaperIntent, 0);
        if (cropAppList.size() > 0) {
            Log.d(TAG, "returns intent");
            return;
        }
        // If the URI is not of the right type, or for some reason the system wallpaper
        // cropper doesn't exist, return null
        Log.d(TAG, "throws Exception");
    }
}