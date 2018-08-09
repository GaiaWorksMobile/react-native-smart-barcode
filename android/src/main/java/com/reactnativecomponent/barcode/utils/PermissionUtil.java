package com.reactnativecomponent.barcode.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class PermissionUtil {
    private static final String E_PERMISSIONS_MISSING = "E_PERMISSIONS_MISSING";
    private static final String E_CALLBACK_ERROR = "E_CALLBACK_ERROR";

    public static void permissionsCheck(final Activity activity, final Promise promise, final List<String> requiredPermissions, final Callable<Void> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> missingPermissions = new ArrayList<>();

            for (String permission : requiredPermissions) {
                int status = ActivityCompat.checkSelfPermission(activity, permission);
                if (status != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }

            if (!missingPermissions.isEmpty()) {

                ((PermissionAwareActivity) activity).requestPermissions(requiredPermissions.toArray(new String[requiredPermissions.size()]), 1, new PermissionListener() {

                    @Override
                    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                        if (requestCode == 1) {

                            for (int grantResult : grantResults) {
                                if (grantResult == PackageManager.PERMISSION_DENIED) {
                                    if (promise != null) {
                                        promise.reject(E_PERMISSIONS_MISSING, "Required permission missing");
                                    }
                                    return true;
                                }
                            }

                            try {
                                if (callback != null) {
                                    callback.call();
                                }
                            } catch (Exception e) {
                                if (promise != null) {
                                    promise.reject(E_CALLBACK_ERROR, "Unknown error", e);
                                }
                            }
                        }

                        return true;
                    }
                });

                return;
            }

            // all permissions granted
            try {
                if (callback != null) {
                    callback.call();
                }
            } catch (Exception e) {
                if (promise != null) {
                    promise.reject(E_CALLBACK_ERROR, "Unknown error", e);
                }
            }
        } else {
            try {
                if (callback != null) {
                    callback.call();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
