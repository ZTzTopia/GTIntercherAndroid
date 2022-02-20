#include "UIHelper.h"
#include "font/IconsMaterialDesign.h"
#include "game/Hook.h"

namespace ui {
    // Close = -1, Expand = 0, Collapse = 1
    uint8_t UIHelper::create_title_bar(std::string title, bool close_button) {
        uint8_t ret{ 0 };

        ImGuiStyle &style = ImGui::GetStyle();
        ImVec2 p = ImGui::GetCursorScreenPos();
        ImGui::GetWindowDrawList()->AddRectFilled(ImVec2(p.x, p.y), ImVec2(p.x + ImGui::GetWindowWidth(), p.y + ImGui::GetCursorPosY() + 19 + ImGui::GetFontSize() * 1.5f), ImColor(47, 54, 64, 255), 8.0f);
        ImGui::SetCursorPosX(ImGui::GetCursorPosX() + 19);
        ImGui::SetCursorPosY(ImGui::GetCursorPosY() + 19);
        ImGui::PushFont(g_ui->get_bold_font());
        ImGui::Text(title.c_str());
        ImGui::PopFont();
        ImGui::SameLine();
        ImGui::SetCursorPosX(ImGui::GetCursorPosX() + ImGui::GetColumnWidth() - ImGui::CalcTextSize("M").x * (close_button ? 2.5f : 1.5f) - ImGui::GetScrollX() - 2 * ImGui::GetStyle().ItemSpacing.x);
        ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(0.0f, 0.0f));
        if (UIHelper::m_title_collapsed[ImGui::GetCurrentWindow()->ID]) {
            if (UIHelper::button(ICON_MD_EXPAND_LESS)) {
                UIHelper::m_title_collapsed[ImGui::GetCurrentWindow()->ID] = false;
            }

            ret = 1;
        }
        else {
            if (UIHelper::button(ICON_MD_EXPAND_MORE)) {
                UIHelper::m_title_collapsed[ImGui::GetCurrentWindow()->ID] = true;
            }
        }
        if (close_button) {
            ImGui::SameLine();
            if (UIHelper::button(ICON_MD_CLOSE)) {
                ret = -1;
            }
        }
        ImGui::PopStyleVar();
        ImGui::SetCursorPosY(ImGui::GetCursorPosY() + 19);
        return ret;
    }

    bool UIHelper::button(const char *label, ImVec2 size) {
        ImGui::PushStyleVar(ImGuiStyleVar_FrameRounding, g_ui->scale_x(8.0f));
        ImGui::PushFont(g_ui->get_small_font());
        bool ret = ImGui::Button(label, size);
        ImGui::PopFont();
        ImGui::PopStyleVar();
        return ret;
    }

    std::unordered_map<ImGuiID, bool> UIHelper::m_title_collapsed;
}
