#include <imgui.h>

#include "JavaWrapper.h"
#include "game/Hook.h"
#include "utilities/Macros.h"

namespace utilities {
    namespace java_wrapper {
        static AAssetManager* g_asset_manager{ nullptr };

        // Helper to retrieve data placed into the assets/ directory (android/app/src/main/assets)
        int get_assets_data(const char* filename, void** outData) {
            size_t num_bytes = 0;
            AAsset* asset_descriptor = AAssetManager_open(g_asset_manager, filename, AASSET_MODE_BUFFER);
            if (asset_descriptor) {
                num_bytes = static_cast<size_t>(AAsset_getLength(asset_descriptor));
                *outData = IM_ALLOC(num_bytes);
                int64_t num_bytes_read = AAsset_read(asset_descriptor, *outData, num_bytes);
                AAsset_close(asset_descriptor);
                IM_ASSERT(num_bytes_read == num_bytes);
            }
            return static_cast<int>(num_bytes);
        }

        void show_soft_keyboard_input(bool show, const char *default_text, bool password) {
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
                    env->CallStaticBooleanMethod(java_class, java_method_id, show, 128, java_string, password);
                }
            }
        }

        void load_font_for_imgui(JNIEnv* env, jclass clazz, jobject asset_manager) {
            g_asset_manager = AAssetManager_fromJava(env, asset_manager);
        }

        void on_floating_mode(JNIEnv *env, jclass clazz, jboolean floating_mode) {
            // Why we need show mod menu while floating mode?
            if (floating_mode) {
                if (g_gui) {
                    delete g_gui;
                    g_gui = nullptr;
                }
                return;
            }

            if (g_gui != nullptr) {
                return;
            }

            g_gui = new gui::Gui{};
            g_gui->init();
        }
    } // namespace java_wrapper
} // namespace utilities