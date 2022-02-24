#pragma once
#include <string>

#include "include/proton/shared/common.h"

namespace game {
    class RemotePlayer {
    public:
        RemotePlayer(int net_id, int user_id, std::string name, CL_Vec2i pos = CL_Vec2i{}, bool is_moderator = false)
                : m_net_id(net_id),
                  m_user_id(user_id),
                  m_pos(pos),
                  m_name(name),
                  m_is_moderator(is_moderator) {}
        ~RemotePlayer() = default;

        int get_user_id() { return m_user_id; }

        std::string get_name() { return m_name; }

    private:
        int m_net_id; // Used to interact with stuff in world
        int m_user_id;
        CL_Vec2i m_pos;
        std::string m_name;
        bool m_is_moderator;
    }; // class RemotePlayer
} // namespace game
