package com.gt.launcher;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.gt.launcher.utils.CrashHandler;
import com.gt.launcher.utils.NativeUtils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {
    private static final String TAG = "GTLauncherAndroid";

    static {
        try {
            // We can load other library here.
            System.loadLibrary("GrowtopiaFix");
            System.loadLibrary("ModMenu");
        }
        catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public static void start(Context context) throws Throwable {
        CrashHandler.init(context, true);

        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo("com.rtsoft.growtopia", 0);

        String libraryPath = applicationInfo.nativeLibraryDir;
        File libraryFile = new File(libraryPath + "/libgrowtopia.so");
        if (!libraryFile.exists()) {
            extractNativeLibrary(context, applicationInfo);
        }

        injectNativeLibrary(context, libraryPath);

        // Start growtopia activity.
        context.startActivity(new Intent(context, GrowtopiaActivity.class));

        // Start floating window service.
        checkOverlayPermission(context);

        // Set version display name to mod menu.
        nativeSetVersionDisplayName(BuildConfig.VERSION_DISPLAY_NAME);
    }

    private static String getExtractedNativeLibraryPath(Context context) {
        String extractedPath = context.getExternalFilesDir(null).getAbsolutePath() + "/extracted";
        String libPath = extractedPath + "/lib";
        String[] libAbi = { "armeabi-v7a", "arm64-v8a" };
        for (String abi : libAbi) {
            File libAbiPath = new File(libPath + "/" + abi);
            if (libAbiPath.exists()) {
                return libAbiPath.getAbsolutePath();
            }
        }

        return "";
    }

    private static void injectNativeLibrary(Context context, String libraryPath) throws Throwable {
        File libraryFile = new File(libraryPath + "/libgrowtopia.so");
        if (!libraryFile.exists()) {
            libraryPath = getExtractedNativeLibraryPath(context);
            libraryFile = new File(libraryPath + "/libgrowtopia.so");
        }

        if (!libraryFile.exists()) {
            Toast.makeText(context, "Failed to find library file", Toast.LENGTH_SHORT).show();
            return;
        }

        copyLibraryToDex(context, libraryPath + "/libgrowtopia.so", "libgrowtopia.so");
        copyLibraryToDex(context, libraryPath + "/libanzu.so", "libanzu.so");

        NativeUtils.installNativeLibraryPath(context.getClassLoader(), new File(context.getDir("dex", 0).getAbsolutePath()), false);
    }

    private static void copyLibraryToDex(Context context, String pathWithFileName, String fileName) throws Exception {
        FileInputStream open = new FileInputStream(pathWithFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(context.getDir("dex", 0).getAbsolutePath() + "/" + fileName);
        byte[] bArr = new byte[1024];
        int bytesRead = 0;
        while (true) {
            int read = open.read(bArr);
            if (read <= 0) {
                fileOutputStream.flush();
                fileOutputStream.close();
                open.close();
                Log.d(TAG, "Writing " + bytesRead + " bytes");
                return;
            }

            bytesRead += read;
            fileOutputStream.write(bArr, 0, read);
        }
    }

    private static void extractNativeLibrary(Context context, ApplicationInfo applicationInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] apkPath = applicationInfo.splitSourceDirs;
            for (String file : apkPath) {
                if (file.contains("split_config.armeabi_v7a.apk") || file.contains("split_config.arm64_v8a.apk")) {
                    try {
                        ZipFile zipFile = new ZipFile(file);
                        zipFile.extractAll(context.getExternalFilesDir(null).getAbsolutePath() + "/extracted/");
                    } catch (ZipException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error extracting library file", Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
            }
        }
    }

    private static void checkOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "Overlay permission is required in order to show mod menu. Restart the game after you allow permission", Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "Overlay permission is required in order to show mod menu. Restart the game after you allow permission", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.getPackageName())));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(1);
                }
            }, 5000);
            return;
        }

        context.startService(new Intent(context, FloatingService.class));
    }

    // https://github.com/LGLTeam/Android-Mod-Menu/blob/master/app/src/main/java/com/android/support/Launcher.java#L39
    public static boolean isNotInApp() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    static native void nativeSetVersionDisplayName(String versionDisplayName);
}
