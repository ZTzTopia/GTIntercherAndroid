#pragma once
#include <enet/enet.h>

#include "Common.h"
#include "Game.h"
#include "ui/UI.h"

extern game::Game *g_game;
extern ui::UI *g_ui;
extern enet_uint16 g_port;

namespace game {
    namespace hook {
        void init();
    } // namespace hook
} // namespace game