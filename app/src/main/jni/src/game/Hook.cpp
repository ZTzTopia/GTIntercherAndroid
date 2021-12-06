#include <dlfcn.h>
#include <android/log.h>

#include "../Main.h"
#include "../include/Dobby/dobby.h"

#define GTS(x) dlsym(g_growtopia_handle, x)

// Fix for printing blank message in the console.
void (*LogMsg)(const char *, ...);
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
                        // GetAppName()
                        KittyMemory::callFunction<const char *>(GTS("_Z10GetAppNamev")), buffer);
}

namespace Game {
    namespace Hook {
        void init() {
            // set Dobby logging level.
            log_set_level(0);

            // LogMsg()
            DobbyHook(GTS("_Z6LogMsgPKcz"), (void *)LogMsg_hook, (void **)&LogMsg);
        }
    } // namespace hook
} // namespace game