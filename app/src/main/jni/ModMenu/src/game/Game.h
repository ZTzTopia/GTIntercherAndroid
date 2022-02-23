#pragma once
#include "Common.h"
#include "World.h"
#include "utilities/Macros.h"

namespace game {
    class Game {
    public:
        Game();
        ~Game() {};

        void init();

        bool get_cheat_state(const std::string &cheat_name);

    public:
        float m_fpsLimit;
        std::vector<_CheatList> m_cheat_list;

        int m_player_when_join;

        World *m_world;
    }; // class Game
} // namespace game