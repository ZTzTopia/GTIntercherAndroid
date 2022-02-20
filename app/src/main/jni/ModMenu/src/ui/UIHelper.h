#pragma once
#include <string>
#include <unordered_map>

#include "GUIManager.h"

namespace ui {
    class UIHelper {
    public:
        static uint8_t create_title_bar(std::string title, bool close_button = true);

        static bool button(const char *label, ImVec2 size = ImVec2());

    private:
        static std::unordered_map<ImGuiID, bool> m_title_collapsed;
    };
}
