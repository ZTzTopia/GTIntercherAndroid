#pragma once
#include <string>
#include <unordered_map>

#include "ImGuiWrapper.h"

namespace ui {
    class UIHelper {
    public:
        // -2 = close, -1 = collapsed, 0 = error, 1 = success
        static int begin_window(ImRect rect, const char* title, bool show_close_button = true, ImGuiWindowFlags flags = 0);

        static bool button(const char *label, ImVec2 size = ImVec2());

    private:
        static std::unordered_map<ImGuiID, bool> m_title_collapsed;
    };
}
