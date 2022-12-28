#include <cstdio>
#include <cstring>
#include <android/log.h>
#include <dobby.h>
#include <KittyMemory.h>

#include "../helper/hook.h"
#include "../ui/ui.h"
#include "../mod_menu.h"

extern ModMenu* g_mod_menu;

INSTALL_HOOK(BaseApp__Draw, void, void* thiz)
{
    static bool once{ false };
    if (!once) {
        auto width =
            KittyMemory::callFunction<float>(DobbySymbolResolver(nullptr, "_Z15GetScreenSizeXfv"));
        auto height =
            KittyMemory::callFunction<float>(DobbySymbolResolver(nullptr, "_Z15GetScreenSizeYfv"));

        g_mod_menu->m_ui = new ui::Ui{ ImVec2{ width, height } };
        once = true;
    }

    g_mod_menu->m_ui->render();
    orig_BaseApp__Draw(thiz);
}

namespace game {
    namespace hook {
        void init()
        {
            // set Dobby logging level.
            log_set_level(0);

            // BaseApp::Draw(void)
            install_hook_BaseApp__Draw("_ZN7BaseApp4DrawEv");
        }
    }
}
