/*
 *
 * DEVELOPER: ZTz
 * PROJECT: GTInternalAndroid
 * VERSION: 0.3
 *
 * ============================================================================
 *
 * Features:
 * Many useful cheats.
 * Debug enet client packet.
 * Multi bot.
 * And many useful features.
 *
 */

#include <android/log.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>

#include "Main.h"
#include "game/Hook.h"
#include "utilities/CrashDump.h"
#include "utilities/JavaWrapper.h"

JavaVM *g_java_vm{ nullptr };
void *g_growtopia_handle{ nullptr };
KittyMemory::ProcMap g_growtopia_map{};

void *main_thread(void *) {
    utilities::crash_dump::init();

    do {
        g_growtopia_map = KittyMemory::getLibraryMap(AY_OBFUSCATE("libgrowtopia.so"));
        sleep(1);
    } while (!g_growtopia_map.isValid());

    do {
        // This is used for dladdr, dlclose, dlerror, dlopen, dlsym, dlvsym.
        // Just open the dynamic library don't load it.
        g_growtopia_handle = dlopen(AY_OBFUSCATE("libgrowtopia.so"), RTLD_NOLOAD);
        sleep(1);
    } while (g_growtopia_handle == nullptr);

    game::hook::init();

    // Now we can exit the thread.
    pthread_exit(nullptr);
}

__unused __attribute__((constructor))
void constructor_main() {
    LOGI(AY_OBFUSCATE("Starting Growtopia ModMenu.. Build time: " __DATE__ " " __TIME__));

    // Create a new thread because we don't want do while loop make main thread
    // stuck.
    pthread_t pthread_id{};
    pthread_create(&pthread_id, nullptr, main_thread, nullptr);
}

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* java_vm, void* reserved) {
    JNIEnv *env{};
    java_vm->GetEnv((void**)&env, JNI_VERSION_1_6);

    // Register your class native methods. Build and ecompile the app and see the signature
    // This is to hide function names from disassembler
    // See more: https://developer.android.com/training/articles/perf-jni#native-libraries

    jclass c = env->FindClass("com/gt/launcher/Main");
    if (c != nullptr) {
        static const JNINativeMethod menuMethods[] = {
            {
                AY_OBFUSCATE("nativeLoadFontForImGui"),
                AY_OBFUSCATE("(Landroid/content/res/AssetManager;)V"),
                reinterpret_cast<void*>(utilities::java_wrapper::load_font_for_imgui)
            },
            {
                AY_OBFUSCATE("nativeOnFloatingMode"),
                AY_OBFUSCATE("(Z)V"),
                reinterpret_cast<void*>(utilities::java_wrapper::on_floating_mode)
            }
        };

        int mm = env->RegisterNatives(c, menuMethods, sizeof(menuMethods) / sizeof(JNINativeMethod));
        if (mm != JNI_OK) {
            LOGE(AY_OBFUSCATE("com/gt/launcher/Main register natives method error."));
            return mm;
        }
    }
    else {
        LOGE(AY_OBFUSCATE("Can not find class com/gt/launcher/Main"));
        return JNI_ERR;
    }

    g_java_vm = java_vm;
    return JNI_VERSION_1_6;
}
