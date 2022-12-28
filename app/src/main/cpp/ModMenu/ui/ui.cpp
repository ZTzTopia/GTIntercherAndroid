#include "ui.h"

namespace ui {
Ui::Ui(ImVec2 display_size)
    : ImGuiWrapper(display_size)
    , m_clear_pos(true) {}

void Ui::render()
{
    ImGuiWrapper::render();

    if (m_clear_pos) {
        ImGuiIO &io = ImGui::GetIO();
        io.MousePos = ImVec2(-FLT_MAX, -FLT_MAX);
        m_clear_pos = false;
    }
}

void Ui::draw()
{
    static bool open = true;
    ImGui::ShowDemoWindow(&open);
}
} // ui
