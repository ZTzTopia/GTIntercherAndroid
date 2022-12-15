package com.gt.launcher.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

// https://github.com/alibaba/freeline/blob/master/freeline-runtime/src/main/java/com/antfortune/freeline/util/ReflectUtil.java
// https://github.com/Tencent/tinker/blob/master/tinker-android/tinker-android-loader-no-op/src/main/java/com/tencent/tinker/loader/shareutil/ShareReflectUtil.java
public class ReflectUtil {
    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static Method findMethod(Object instance, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);

                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method "
            + name
            + " with parameters "
            + Arrays.asList(parameterTypes)
            + " not found in " + instance.getClass());
    }
}
