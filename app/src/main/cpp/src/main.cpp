#include <chrono>
#include <thread>
#include <unistd.h>
#include <android/log.h>
#include <dlfcn.h>
#include <pthread.h>

#include "game/hook.h"

__unused __attribute__((constructor))
void constructor_main() {
    // Create a new thread because we don't want do while loop make main thread
    // stuck.
    auto thread = std::thread([]() {
        // Wait until Growtopia native library loaded.
        do {
            std::this_thread::sleep_for(std::chrono::milliseconds{ 32 });
        } while (dlopen("libgrowtopia.so", RTLD_NOLOAD) == nullptr);

        // Starting hook Growtopia function.
        game::hook::init();
    });

    // Don't forget to detach the thread from the main thread.
    thread.detach();
}
