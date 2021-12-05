package com.gt.launcher;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NativeUtils {
    private static final String TAG = "GTLauncherAndroid";

    // https://github.com/alibaba/freeline/blob/master/freeline-runtime/src/main/java/com/antfortune/freeline/util/NativeUtils.java#L91
    public static void installNativeLibraryPath(ClassLoader classLoader, File folder, boolean reverse)
            throws Throwable {
        Log.i(TAG, "installNativeLibraryPath folder:" + folder.getAbsolutePath());
        if (!folder.exists()) {
            Log.e(TAG, "installNativeLibraryPath, folder " + folder + " is illegal");
            return;
        }

        // android o sdk_int 26
        // for android o preview sdk_int 25
        if ((Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT != 0)
                || Build.VERSION.SDK_INT > 25) {
            try {
                V25.install(classLoader, folder, reverse);
                return;
            } catch (Throwable throwable) {
                // install fail, try to treat it as v23
                // some preview N version may go here
                Log.e(TAG, "installNativeLibraryPath, v25 fail, sdk: " + Build.VERSION.SDK_INT + ", try to fallback to V23", throwable);
                V23.install(classLoader, folder, reverse);
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            try {
                V23.install(classLoader, folder, reverse);
            } catch (Throwable throwable) {
                // install fail, try to treat it as v14
                Log.e(TAG, "installNativeLibraryPath, v23 fail, sdk: " + Build.VERSION.SDK_INT + ", try to fallback to V14", throwable);
                V14.install(classLoader, folder, reverse);
            }
        } else {
            V14.install(classLoader, folder, reverse);
        }
    }

    private static final class V14 {
        private static void install(ClassLoader classLoader, File folder, boolean reverse) throws Throwable {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);

            if (reverse) {
                expandFieldArray(dexPathList, "nativeLibraryDirectories", new File[]{folder});
            } else {
                ReflectUtil.expandFieldArray(dexPathList, "nativeLibraryDirectories", new File[]{folder});
            }
        }
    }

    /**
     * Replace the value of a field containing a non null array, by a new array containing the
     * elements of the original array plus the elements of extraElements.
     *
     * @param instance      the instance whose field is to be modified.
     * @param fieldName     the field to modify.
     * @param extraElements elements to append at the end of the array.
     */
    private static void expandFieldArray(Object instance, String fieldName,
                                         Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field jlrField = ReflectUtil.findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    private static final class V23 {
        private static void install(ClassLoader classLoader, File folder, boolean reverse) throws Throwable {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);

            Field nativeLibraryDirectories = ReflectUtil.findField(dexPathList, "nativeLibraryDirectories");

            List<File> libDirs = (List<File>) nativeLibraryDirectories.get(dexPathList);
            if (!reverse) {
                libDirs.add(0, folder);
            }
            Field systemNativeLibraryDirectories =
                    ReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories");
            List<File> systemLibDirs = (List<File>) systemNativeLibraryDirectories.get(dexPathList);
            Method makePathElements =
                    ReflectUtil.findMethod(dexPathList, "makePathElements", List.class, File.class, List.class);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            libDirs.addAll(systemLibDirs);
            if (reverse) {
                libDirs.add(folder);
            }
            Object[] elements = (Object[]) makePathElements.
                    invoke(dexPathList, libDirs, null, suppressedExceptions);
            Field nativeLibraryPathElements = ReflectUtil.findField(dexPathList, "nativeLibraryPathElements");
            nativeLibraryPathElements.setAccessible(true);
            nativeLibraryPathElements.set(dexPathList, elements);
        }
    }

    private static final class V25 {
        private static void install(ClassLoader classLoader, File folder, boolean reverse) throws Throwable {
            Field pathListField = ReflectUtil.findField(classLoader, "pathList");
            Object dexPathList = pathListField.get(classLoader);

            Field nativeLibraryDirectories = ReflectUtil.findField(dexPathList, "nativeLibraryDirectories");

            List<File> libDirs = (List<File>) nativeLibraryDirectories.get(dexPathList);
            if (!reverse) {
                libDirs.add(0, folder);
            }
            Field systemNativeLibraryDirectories =
                    ReflectUtil.findField(dexPathList, "systemNativeLibraryDirectories");
            List<File> systemLibDirs = (List<File>) systemNativeLibraryDirectories.get(dexPathList);
            Method makePathElements =
                    ReflectUtil.findMethod(dexPathList, "makePathElements", List.class);
            libDirs.addAll(systemLibDirs);
            if (reverse) {
                libDirs.add(folder);
            }
            Object[] elements = (Object[]) makePathElements.
                    invoke(dexPathList, libDirs);
            Field nativeLibraryPathElements = ReflectUtil.findField(dexPathList, "nativeLibraryPathElements");
            nativeLibraryPathElements.setAccessible(true);
            nativeLibraryPathElements.set(dexPathList, elements);
        }
    }
}
