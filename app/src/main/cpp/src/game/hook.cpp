#include <cstdio>
#include <cstring>
#include <android/log.h>
#include <dobby.h>

#define INSTALL_HOOK(lib, name, fn_ret_t, fn_args_t...)                                                                    \
    fn_ret_t (*orig_##name)(fn_args_t);                                                                                    \
    fn_ret_t fake_##name(fn_args_t);                                                                                       \
    static void install_hook_##name() {                                                                                    \
        void *sym_addr = DobbySymbolResolver(lib, #name);                                                                  \
        DobbyHook(sym_addr, (dobby_dummy_func_t)fake_##name, (dobby_dummy_func_t*)&orig_##name);                           \
    }                                                                                                                      \
    fn_ret_t fake_##name(fn_args_t)

#define INSTALL_HOOK_NO_LIB(name, fn_ret_t, fn_args_t...)                                                                  \
    INSTALL_HOOK(nullptr, name, fn_ret_t, fn_args_t)

// Fix for printing blank message in the console.
INSTALL_HOOK_NO_LIB(_Z6LogMsgPKcz, void, const char* msg, ...)
{
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
        reinterpret_cast<const char* (__cdecl*)()>(DobbySymbolResolver(nullptr, "_Z10GetAppNamev"))(),
        "%s", buffer
    );
}

// Fix for 0ms timeout causing game lag.
INSTALL_HOOK_NO_LIB(enet_host_service, int, void* host, void* event, uint32_t timeout)
{
    return orig_enet_host_service(host, event, timeout != 0 ? timeout : 16);
}

namespace game {
    namespace hook {
        void init()
        {
            // set Dobby logging level.
            log_set_level(0);

            // LogMsg(char const*,...)
            install_hook__Z6LogMsgPKcz();

            // enet_host_service
            install_hook_enet_host_service();
        }
    }
}
