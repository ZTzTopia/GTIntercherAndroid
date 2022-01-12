#pragma once
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

namespace utilities {
    namespace java_wrapper {
        int get_assets_data(const char* filename, void** outData);
        void show_soft_keyboard_input(bool show, const char *default_text, bool password);
        void load_font_for_imgui(JNIEnv* env, jclass clazz, jobject asset_manager);
        void on_floating_mode(JNIEnv *env, jclass clazz, jboolean floating_mode);
    } // namespace java_wrapper
} // namespace utilities