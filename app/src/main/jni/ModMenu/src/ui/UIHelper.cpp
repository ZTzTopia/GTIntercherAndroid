#include "UIHelper.h"
#include "font/IconsMaterialDesign.h"
#include "game/Hook.h"

namespace ui {
    struct WindowData {
        ImGuiID m_id;
        bool m_collapsed;
    };

    static std::vector<WindowData *> g_window_data;

    int UIHelper::begin_window(ImRect rect, const char *title, bool show_close_button, ImGuiWindowFlags flags) {
        ImGuiID id = ImGui::GetID(title);
        auto it = std::find_if(g_window_data.begin(), g_window_data.end(), [&](WindowData *data) {
            return data->m_id == id;
        });

        if (it == g_window_data.end()) {
            g_window_data.push_back(new WindowData{
                id,
                false
            });
            it = g_window_data.end() - 1;
        }

        auto window_data = *it;

        ImGui::SetNextWindowPos(ImVec2(rect.Min.x, rect.Min.y), ImGuiCond_Once);
        ImGui::SetNextWindowSize(ImVec2(rect.Max.x, window_data->m_collapsed ? ImGui::GetFontSize() * 2.0f : rect.Max.y));

        bool ret = ImGui::Begin(title, nullptr, flags | ImGuiWindowFlags_NoTitleBar | ImGuiWindowFlags_NoResize | ImGuiWindowFlags_NoScrollbar);
        if (ret) {
            ImGuiStyle &style = ImGui::GetStyle();
            ImVec2 pos = ImGui::GetCursorScreenPos();

            ImGui::GetWindowDrawList()->AddRectFilled(ImVec2(pos.x, pos.y), ImVec2(pos.x + ImGui::GetWindowWidth(), pos.y + ImGui::GetFontSize() * 2.0f), ImColor(47, 54, 64, 255), 8.0f);

            ImGui::SetCursorPosX(ImGui::GetCursorPosX() + ImGui::GetFontSize() / 2.0f);
            ImGui::SetCursorPosY(ImGui::GetCursorPosY() + ImGui::GetFontSize() / 2.0f);

            ImGui::PushFont(g_ui->get_bold_font());
            ImGui::Text(title);
            ImGui::PopFont();

            ImGui::SameLine();

            ImGui::SetCursorPosX(ImGui::GetCursorPosX() + ImGui::GetColumnWidth() - ImGui::CalcTextSize("M").x * (show_close_button ? 3.15f : 1.15f) - ImGui::GetScrollX() - 2 * ImGui::GetStyle().ItemSpacing.x);
            ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(0.0f, 0.0f));
            if (window_data->m_collapsed) {
                if (UIHelper::button(ICON_MD_EXPAND_LESS, ImVec2(ImGui::GetFontSize(), ImGui::GetFontSize()))) {
                    window_data->m_collapsed = false;
                }
            }
            else {
                if (UIHelper::button(ICON_MD_EXPAND_MORE, ImVec2(ImGui::GetFontSize(), ImGui::GetFontSize()))) {
                    window_data->m_collapsed = true;
                }
            }
            if (show_close_button) {
                ImGui::SameLine();
                if (UIHelper::button(ICON_MD_CLOSE, ImVec2(ImGui::GetFontSize(), ImGui::GetFontSize()))) {
                    ret = -1;
                }
            }
            ImGui::PopStyleVar();
        }

        if (window_data->m_collapsed) {
            return -2;
        }

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
}
