#include <cstdio>
#include <cstring>
#include <dlfcn.h>
#include <android/log.h>
#include <dobby.h>

// Fix for printing blank message in the console.
void (*LogMsg)(const char *, ...);
void LogMsg_hook(const char *msg, ...) {
    if (msg[0] == '\0') {
        return;
    }

    char buffer[0x1000u];
    va_list va{};
    va_start(va, msg);
    std::memset(buffer, 0, sizeof(buffer));
    std::vsnprintf(buffer, 0x1000u, msg, va);

    // Double check.
    if (buffer[0] == '\0') {
        return;
    }

    __android_log_print(
        ANDROID_LOG_INFO,
        reinterpret_cast<const char* (__cdecl *)()>(DobbySymbolResolver(nullptr, "_Z10GetAppNamev"))(),
        "%s", buffer
    );
}

namespace game {
    namespace hook {
        void init() {
            // set Dobby logging level.
            log_set_level(0);

            // LogMsg()
            DobbyHook(
                DobbySymbolResolver(nullptr, "_Z6LogMsgPKcz"),
                (dobby_dummy_func_t)LogMsg_hook,
                (dobby_dummy_func_t*)&LogMsg
            );
        }
    } // namespace hook
} // namespace game
