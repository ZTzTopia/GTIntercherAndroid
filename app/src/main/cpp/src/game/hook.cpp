#include <cstdio>
#include <cstring>
#include <string>
#include <android/log.h>
#include <dobby.h>

#include "../helper/gnu_string.h"

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

INSTALL_HOOK_NO_LIB(_Z10SendPacket15eNetMessageTypeRKSsP9_ENetPeer, void, int v1, gnu_string& v2, void* v3)
{
    __android_log_print(
        ANDROID_LOG_INFO,
        "GTL.Native",
        "type: %d, length: %lu, data: %s",
        v1,
        v2.length(),
        v2.data()
    );
    orig__Z10SendPacket15eNetMessageTypeRKSsP9_ENetPeer(v1, v2, v3);
}

struct BoostSignal {
    void* pad; // 0
    void* pad2; // 8
    void* pad3; // 16
    // ARM64 size!
};

struct BaseApp {
    BoostSignal pad[18]; // 0
    void* pad2; // 432
    bool consoleVisible; // 440
    bool fpsVisible; // 441
    // ARM64 size!
};

INSTALL_HOOK_NO_LIB(_ZN7BaseApp4DrawEv, void, BaseApp* v1)
{
    v1->fpsVisible = true;
    orig__ZN7BaseApp4DrawEv(v1);
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

            // SendPacket(eNetMessageType,std::string const&,_ENetPeer *)
            install_hook__Z10SendPacket15eNetMessageTypeRKSsP9_ENetPeer();

            // BaseApp::Draw(void)
            install_hook__ZN7BaseApp4DrawEv();
        }
    }
}
