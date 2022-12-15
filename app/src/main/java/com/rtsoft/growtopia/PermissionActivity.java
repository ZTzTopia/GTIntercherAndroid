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

public class PermissionActivity extends Activity {
    public static Activity mainActivity;
    private static PermissionActivity _pa;
    private static boolean isActive;
    int checkPermissionIteration;
    String[] requestablePermissions;
    String[][] requiredPermissions;
    boolean shouldRequestForPermissions = false;

    public PermissionActivity() {
        String[][] permissions = {
            new String[]{
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "Storage",
                "The game needs this permission to write your progress to the device. The game cannot run without this permission."
            }/*,
            new String[] {
                "android.permission.VIBRATE",
                "Vibrate",
                "The game needs this permission to write your progress to the device. The game cannot run without this permission."
            },
            new String[] {
                "android.permission.ACCESS_FINE_LOCATION",
                "Access fine location",
                "The game needs this permission to write your progress to the device. The game cannot run without this permission."
            }*/
        };

        requiredPermissions = permissions;
        requestablePermissions = new String[permissions.length];
        checkPermissionIteration = 0;
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (isActive) {
            Log.d("PermissionActivity", "Active: Finishing.");
            finish();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("PermissionActivity", "API Higher: Finishing.");
            finish();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d("PermissionActivity", "API Lower: Finishing.");
            finish();
        } else {
            Log.d("PermissionActivity", "Checking Permissions.");
            _pa = this;
            isActive = true;
            checkPermissions();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void checkPermissions() {
        checkPermissionIteration++;
        if (checkPermissionIteration >= 3) {
            permissionPopup(
                "Growtopia Shutting Down",
                "Sorry Growtopia can not be played without these permissions.",
                true,
                true
            );
        }

        shouldRequestForPermissions = false;
        String str = "";
        boolean z = false;
        for (int i2 = 0; i2 < requiredPermissions.length; i2++) {
            if (ActivityCompat.checkSelfPermission(
                getApplicationContext(),
                requiredPermissions[i2][0]
            ) == -1) {
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

        if (shouldRequestForPermissions && checkPermissionIteration == 1) {
            ActivityCompat.requestPermissions(this, requestablePermissions, 100);
        } else if (shouldRequestForPermissions && checkPermissionIteration == 2) {
            permissionPopup("Permission Required", str, z, false);
        }
    }

    private void permissionPopup(String str, String str2, boolean z, final boolean z2) {
        AlertDialog create = new AlertDialog.Builder(
            this,
            android.R.style.Theme_Material_Dialog_Alert
        ).create();
        String str3 = str2;
        if (z) {
            str3 = str2 + " You can enable missing permissions in the permission section of the application settings.";
        }

        create.setTitle(str);
        create.setMessage(Html.fromHtml(str3));
        create.setIcon(android.R.drawable.ic_dialog_alert);

        if (z) {
            create.setButton(-3, "Settings", (dialogInterface, i) -> {
                Intent intent = new Intent(
                    "android.settings.APPLICATION_DETAILS_SETTINGS",
                    Uri.fromParts("package",
                        PermissionActivity.this.getApplicationContext().getPackageName(),
                        null
                    )
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                ActivityCompat.requestPermissions(
                    PermissionActivity._pa,
                    PermissionActivity.this.requestablePermissions,
                    100
                );
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
}
