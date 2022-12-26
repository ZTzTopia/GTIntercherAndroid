#include <cstdio>
#include <cstring>
#include <android/log.h>
#include <dobby.h>

#include "../helper/gnu_string.h"

#define INSTALL_HOOK(fn_name_t, fn_ret_t, fn_args_t...)                                                                    \
    fn_ret_t (*orig_##fn_name_t)(fn_args_t);                                                                               \
    fn_ret_t fake_##fn_name_t(fn_args_t);                                                                                  \
    static void install_hook_##fn_name_t(void* sym_addr)                                                                   \
    {                                                                                                                      \
        DobbyHook(sym_addr, (dobby_dummy_func_t)fake_##fn_name_t, (dobby_dummy_func_t*)&orig_##fn_name_t);                 \
    }                                                                                                                      \
    static void install_hook_##fn_name_t(const char* lib, const char* name)                                                \
    {                                                                                                                      \
        void *sym_addr = DobbySymbolResolver(lib, name);                                                                   \
        install_hook_##fn_name_t(sym_addr);                                                                                \
    }                                                                                                                      \
    static void install_hook_##fn_name_t(const char* name)                                                                 \
    {                                                                                                                      \
        install_hook_##fn_name_t(nullptr, name);                                                                           \
    }                                                                                                                      \
    fn_ret_t fake_##fn_name_t(fn_args_t)

// Fix for printing blank message in the console.
INSTALL_HOOK(LogMsg, void, const char* msg, ...)
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

INSTALL_HOOK(SendPacket, void, int message_type, gnu::string& message, void* enet_peer)
{
    __android_log_print(
        ANDROID_LOG_INFO,
        "GTL.Native",
        "type: %d, length: %lu, data: %s",
        message_type,
        message.length(),
        message.data()
    );
    orig_SendPacket(message_type, message, enet_peer);
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

INSTALL_HOOK(BaseApp__Draw, void, BaseApp* thiz)
{
    thiz->fpsVisible = true;
    orig_BaseApp__Draw(thiz);
}

namespace game {
    namespace hook {
        void init()
        {
            // set Dobby logging level.
            log_set_level(0);

            // LogMsg(char const*,...)
            install_hook_LogMsg("_Z6LogMsgPKcz");

            // SendPacket(eNetMessageType,std::string const&,_ENetPeer *)
            install_hook_SendPacket("_Z10SendPacket15eNetMessageTypeRKSsP9_ENetPeer");

            // BaseApp::Draw(void)
            install_hook_BaseApp__Draw("_ZN7BaseApp4DrawEv");
        }
    }
}
