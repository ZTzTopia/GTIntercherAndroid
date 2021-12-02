package com.rtsoft.growtopia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.drive.DriveFile;

public class PermissionActivity extends Activity {
    private static PermissionActivity _pa;
    private static boolean isActive;
    public static Activity mainActivity;
    int checkPermissionIteration;
    String[] requestablePermissions;
    String[][] requiredPermissions;
    boolean shouldRequestForPermissions = false;

    public PermissionActivity() {
        String[][] strArr = {new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "Storage", "The game needs this permission to write your progress to the device. The game cannot run without this permission."}};
        requiredPermissions = strArr;
        requestablePermissions = new String[strArr.length];
        checkPermissionIteration = 0;
    }

    private void checkPermissions() {
        int i = checkPermissionIteration + 1;
        checkPermissionIteration = i;
        if (i == 3) {
            permissionPopup("Growtopia Shutting Down", "Sorry Growtopia can not be played without these permissions.", true, true);
        }

        shouldRequestForPermissions = false;
        String str = "";
        boolean z = false;
        for (int i2 = 0; i2 < requiredPermissions.length; i2++) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), requiredPermissions[i2][0]) == -1) {
                String[] strArr = requestablePermissions;
                String[][] strArr2 = requiredPermissions;
                strArr[i2] = strArr2[i2][0];
                String str2 = str;
                if (checkPermissionIteration == 2) {
                    z = !ActivityCompat.shouldShowRequestPermissionRationale(this, strArr2[i2][0]);
                    str2 = str + "<b>" + requiredPermissions[i2][1] + "</b><br>" + requiredPermissions[i2][2] + "<br><br>";
                }
                shouldRequestForPermissions = true;
                str = str2;
            } else {
                requestablePermissions[i2] = "";
            }
        }

        if (!shouldRequestForPermissions) {
            isActive = false;
            finish();
        }

        boolean z2 = shouldRequestForPermissions;
        if (z2 && checkPermissionIteration == 1) {
            ActivityCompat.requestPermissions(this, requestablePermissions, 100);
        } else if (z2 && checkPermissionIteration == 2) {
            permissionPopup("Permission Required", str, z, false);
        }
    }

    private void permissionPopup(String str, String str2, boolean z, final boolean z2) {
        AlertDialog create = new AlertDialog.Builder(this, 16974374).create();
        String str3 = str2;
        if (z) {
            str3 = str2 + " You can enable missing permissions in the permission section of the application settings.";
        }

        create.setTitle(str);
        create.setMessage(Html.fromHtml(str3));
        create.setIcon(17301543); // TODO: Change.

        if (z) {
            create.setButton(-3, "Settings", (dialogInterface, i) -> {
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", PermissionActivity.this.getApplicationContext().getPackageName(), null));
                intent.addFlags(DriveFile.MODE_READ_ONLY);
                PermissionActivity.this.getApplicationContext().startActivity(intent);
                if (PermissionActivity.mainActivity != null) {
                    PermissionActivity.mainActivity.finish();
                    PermissionActivity.mainActivity = null;
                    PermissionActivity.this.finish();
                    System.exit(0);
                }
            });
        }

        create.setButton(-1, "Ok", (dialogInterface, i) -> {
            if (!z2) {
                dialogInterface.cancel();
                Log.d("PermissionActivity", "Requesting Permissions Again.");
                ActivityCompat.requestPermissions(PermissionActivity._pa, PermissionActivity.this.requestablePermissions, 100);
            } else if (PermissionActivity.mainActivity != null) {
                PermissionActivity.mainActivity.finish();
                PermissionActivity.mainActivity = null;
                PermissionActivity.this.finish();
                System.exit(0);
            }
        });

        create.setCanceledOnTouchOutside(false);
        create.setCancelable(false);
        create.show();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (isActive) {
            Log.d("PermissionActivity", "Active: Finishing.");
            finish();
        } else if (Build.VERSION.SDK_INT < 23) {
            Log.d("PermissionActivity", "API Lower: Finishing.");
            finish();
        } else {
            Log.d("PermissionActivity", "Checking Permissions.");
            _pa = this;
            isActive = true;
            checkPermissions();
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        int length = strArr.length;
        boolean z = false;
        for (int i2 = 0; i2 < length; i2++) {
            if (iArr[i2] == -1) {
                z = true;
                break;
            }
        }

        if (z) {
            checkPermissions();
            return;
        }

        isActive = false;
        finish();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }
}
