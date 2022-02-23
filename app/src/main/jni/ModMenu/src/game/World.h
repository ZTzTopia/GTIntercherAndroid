#pragma once
#include <string>
#include <vector>

#include "LocalPlayer.h"

namespace game {
    class World {
    public:
        World()
            : m_name(""),
            m_owner_name(""),
            m_local_player(nullptr) {}
        ~World() = default;

        std::string get_name() { return m_name; }
        void set_name(const std::string& name) { m_name = name; }

        std::string get_owner_name() { return m_owner_name; }
        void set_owner_name(const std::string& owner_name) { m_owner_name = owner_name; }

        LocalPlayer* get_local_player() { return m_local_player; }
        void set_local_player(LocalPlayer* local_player) { m_local_player = local_player; }

    private:
        std::string m_name;
        std::string m_owner_name;
        LocalPlayer *m_local_player;
    }; // class World
} // namespace game