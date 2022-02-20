#pragma once
#include <string>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

namespace utilities {
    namespace java_wrapper {
        void show_soft_input(bool show, const char *default_text, bool password, int max_length = 120);
    } // namespace java_wrapper
} // namespace utilities

extern std::string g_version_display_name_string;
