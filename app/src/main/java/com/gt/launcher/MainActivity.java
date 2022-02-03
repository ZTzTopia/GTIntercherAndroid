package com.gt.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends Activity {
    private static final String TAG = "GTLauncherAndroid";
    public boolean hasLaunched = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        if (!hasLaunched) {
            try {
                getPackageManager().getPackageInfo("com.rtsoft.growtopia", 0);
                Main.start(this);
                hasLaunched = true;
            }
            catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Growtopia application not found");
                        alertDialog.setMessage("This launcher need original Growtopia application from playstore to run, please donwload it from playstore and re-open the launcher.");
                        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                System.exit(1);
                            }
                        });
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                    }
                });
            }
            catch (Throwable th) {
                th.printStackTrace();
                Toast.makeText(context, "Error: " + th.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
