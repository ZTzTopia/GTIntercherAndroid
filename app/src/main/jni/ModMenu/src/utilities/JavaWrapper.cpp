#include <imgui.h>

#include "JavaWrapper.h"
#include "game/Hook.h"
#include "utilities/Macros.h"

namespace utilities {
    namespace java_wrapper {
        void show_soft_keyboard_input(bool show, const char *default_text, bool password, int max_length) {
            JNIEnv *env{};
            g_java_vm->GetEnv((void**)&env, JNI_VERSION_1_6);
            jclass java_class = env->FindClass("com/gt/launcher/Launch");
            if (java_class == nullptr) {
                LOGE("Class com/gt/launcher/Launch not found.");
            }
            else {
                jmethodID java_method_id = env->GetStaticMethodID(java_class, "toggleKeyboard", "(ZILjava/lang/String;Z)Z");
                if (java_method_id == nullptr) {
                    LOGE("method static void toggleKeyboard not found.");
                }
                else {
                    jstring java_string = env->NewStringUTF(default_text);
                    env->CallStaticBooleanMethod(java_class, java_method_id, show, max_length, java_string, password);
                }
            }
        }
    } // namespace java_wrapper
} // namespace utilities