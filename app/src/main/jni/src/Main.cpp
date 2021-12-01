#include <android/log.h>
#include <dlfcn.h>
#include <pthread.h>
#include <unistd.h>

#include "Main.h"
#include "game/Hook.h"

void *g_growtopia_handle{ nullptr };

void *main_thread(void *) {
    do {
        // This is used for dladdr, dlclose, dlerror, dlopen, dlsym, dlvsym.
        // Just open the dynamic library don't load it.
        g_growtopia_handle = dlopen("libgrowtopia.so", RTLD_NOLOAD);
        sleep(1);
    } while (g_growtopia_handle == nullptr);

    game::hook::init();

    // Now we can exit the thread.
    pthread_exit(nullptr);
}

__unused __attribute__((constructor))
void constructor_main() {
    // Create a new thread because we don't want do while loop make main thread
    // stuck.
    pthread_t pthread_id{};
    pthread_create(&pthread_id, nullptr, main_thread, nullptr);
}