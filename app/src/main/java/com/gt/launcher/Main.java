/*
 *
 * TODO:
 * Fix Couldn't load surface (from assets), ok it looks like growtopia original too.
 * Fix save data cache path to this application data not growtopia data. -> Done
 * Fix growtopia and anzu java folder code, lazy to fix :D (but it works)
 * Fix tapjoy. -> Done
 * Fix IAP purchase . -> I think we need to change package name to com.rtsoft.growtopia?
 * Fix crash after resume the game. -> I think it's fixed
 *
 */

package com.gt.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class Main extends Activity {
    private final String TAG = "GTLauncher";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We can load other library here.
        System.loadLibrary("GrowtopiaFix");

        try {
            getPackageManager().getPackageInfo("com.rtsoft.growtopia", 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Growtopia application not found.");

            runOnUiThread(() -> {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this);
                dialog.setTitle("Growtopia application not found.")
                        .setMessage("This launcher need original Growtopia application from playstore to run.")
                        .setNegativeButton("Close", (dialoginterface, which) -> {
                            Process.killProcess(Process.myPid());
                            System.exit(2);
                        }).create();

                dialog.show();
            });
            return;
        }

        startActivity(new Intent(this, Launch.class));
        finish();
    }
}
