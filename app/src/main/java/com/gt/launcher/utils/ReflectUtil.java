package com.gt.launcher.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

// https://github.com/alibaba/freeline/blob/master/freeline-runtime/src/main/java/com/antfortune/freeline/util/ReflectUtil.java
public class ReflectUtil {
    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while(clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static Method findMethod(Object instance, String name, Class... parameterTypes) throws NoSuchMethodException {
        Class clazz = instance.getClass();

        while(clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if(!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException var5) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }

    public static void expandFieldArray(Object instance, String fieldName, Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[])((Object[])jlrField.get(instance));
        Object[] combined = (Object[])((Object[]) Array.newInstance(original.getClass().getComponentType(), original.length + extraElements.length));
        System.arraycopy(extraElements, 0, combined, 0, extraElements.length);
        System.arraycopy(original, 0, combined, extraElements.length, original.length);
        jlrField.set(instance, combined);
    }
}