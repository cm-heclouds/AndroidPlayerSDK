package com.ont.player.sample.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.PermissionChecker;

public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    public static final int PERMISSIONS_STORAGE_CODE = 101;
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    public static boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasSelfPermission(Context context, String[] permissions) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            int targetSdkVersion = -1;
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (targetSdkVersion >= 23) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                for (String permission : permissions) {
                    if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        result = false;
                    }
                }
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                for (String permission : permissions) {
                    if (PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 验证权限，未获得就请求一次
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAndRequestPermission(Activity activity, String[] permissions, int code) {
        if (hasSelfPermission(activity, permissions)) {
            return true;
        } else {
            activity.requestPermissions(permissions, code);
            return false;
        }
    }

    /**
     * 验证权限
     */
    public static boolean checkPermission(Activity activity, String[] permissions) {
        if (hasSelfPermission(activity, permissions)) {
            return true;
        }
        return false;
    }

    public static boolean isM(Context context) {
        int targetSdkVersion = -1;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return targetSdkVersion >= 23 && Build.VERSION.SDK_INT >= 23;
    }
}
