#include <dlfcn.h>
#include <android/log.h>

#include "../Main.h"
#include "../include/KittyMemory/KittyMemory.h"

#define GTS(x) dlsym(g_growtopia_handle, x)

#ifdef __arm__
#include "../include/Substrate/SubstrateHook.h"
#define HOOK(a, b, c) MSHookFunction(a, b, c)
#elif __aarch64__
#include "../include/And64InlineHook/And64InlineHook.hpp"
#define HOOK(a, b, c) A64HookFunction(a, b, c)
#endif

// Fix for android 10-11 crash because of the wrong bundle prefix and bundle name (Package name).
const char* GetBundlePrefix_hook() {
    return "com.gt.";
}

const char* GetBundleName_hook() {
    return "launcher";
}

// Fix for printing blank message in the console.
void (*LogMsg)(const char *msg, ...);
void LogMsg_hook(const char *msg, ...) {
    if (msg[0] == '\0') {
        return;
    }

    char buffer[0x1000u];
    va_list va{};
    va_start(va, msg);
    memset(buffer, 0, sizeof(buffer));
    vsnprintf(buffer, 0x1000u, msg, va);

    // Double check.
    if (buffer[0] == '\0') {
        return;
    }

    __android_log_print(ANDROID_LOG_INFO,
                        KittyMemory::callFunction<const char *>(GTS("_Z10GetAppNamev")), buffer);
}

namespace game {
    namespace hook {
        void init() {
            // GetBundlePrefix()
            HOOK(GTS("_Z15GetBundlePrefixv"), (void *)GetBundlePrefix_hook, NULL);

            // GetBundleName()
            HOOK(GTS("_Z13GetBundleNamev"), (void *)GetBundleName_hook, NULL);

            // LogMsg()
            HOOK(GTS("_Z6LogMsgPKcz"), (void *)LogMsg_hook, (void **)&LogMsg);
        }
    } // namespace hook
} // namespace game