#include "LuaLog.h"
#include "Gui.h"
#include "Utils.h"
#include "font/IconsFontAwesome5.h"
#include "game/Hook.h"

namespace gui {
    LuaLog::LuaLog()
        : m_visible(false)
    {
        m_log_entry.clear();
    }

    void LuaLog::render() {
        if (!m_visible) {
            return;
        }

        ImGuiStyle &style = ImGui::GetStyle();

        ImVec2 basedWindowSize = ImVec2(g_gui->m_screenSize.x / 2.5f, g_gui->m_screenSize.y / 2.0f);
        ImGui::SetNextWindowSize(basedWindowSize);

        ImGui::Begin("##LuaLog", nullptr, ImGuiWindowFlags_NoDecoration);
        {
            // Title bar.
            ImVec2 p = ImGui::GetCursorScreenPos();
            ImGui::GetWindowDrawList()->AddRectFilled(
                    ImVec2(p.x, p.y),
                    ImVec2(p.x + ImGui::GetWindowWidth(), p.y + ImGui::GetCursorPosY() + 19 + style.ItemSpacing.y + ImGui::GetFontSize() * 1.5f),
                    ImColor(47, 54, 64, 255),
                    8.0f);
            ImGui::SetCursorPosX(ImGui::GetCursorPosX() + 19);
            ImGui::SetCursorPosY(ImGui::GetCursorPosY() + 19);
            ImGui::PushFont(g_gui->m_bold_font);
            ImGui::Text("Lua log");
            ImGui::PopFont();
            ImGui::SameLine();
            ImGui::SetCursorPosX(ImGui::GetCursorPosX() + ImGui::GetColumnWidth() - ImGui::CalcTextSize(ICON_FA_TIMES).x * 2.0f - ImGui::GetScrollX() - 2 * ImGui::GetStyle().ItemSpacing.x);
            ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(0.0f, 0.0f));
            if (utils::button(ICON_FA_TIMES, ImVec2(ImGui::CalcTextSize(ICON_FA_TIMES).x * 2.0f, ImGui::GetFontSize() * 1.2f))) {
                toggle();
            }
            ImGui::PopStyleVar();
            ImGui::SetCursorPosY(ImGui::GetCursorPosY() + 36);

            ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, ImVec2(ImGui::GetFontSize() * 0.5f, ImGui::GetFontSize() * 0.5f));
            ImGui::PushStyleVar(ImGuiStyleVar_ChildRounding, g_gui->m_scale.x * 8.0f);
            ImGui::PushStyleColor(ImGuiCol_ChildBg, ImVec4(45.0f / 255.0f, 49.0f / 255.0f, 58.0f / 255.0f, 1.0f));
            ImGui::Dummy(ImVec2(style.FramePadding.x, 0.0f));
            ImGui::SameLine(0.0f, 0.0f);
            ImGui::BeginChild("##CheatChild", ImVec2(ImGui::GetWindowSize().x - style.FramePadding.x * 2.0f, ImGui::GetWindowSize().y * 0.71f), false, ImGuiWindowFlags_AlwaysUseWindowPadding);

            if (!m_log_entry.empty()) {
                std::string full_log_entry{};
                for (auto &log_entry : m_log_entry) {
                    full_log_entry += log_entry.m_message + "\n";

                    const char *icon = ICON_FA_INFO_CIRCLE;
                    ImColor color = ImColor(92, 92, 255, 255);
                    if (log_entry.m_type == LogEntry::WARNING) {
                        icon = ICON_FA_EXCLAMATION_CIRCLE;
                        color = ImColor(255, 155, 32, 255);
                    }
                    else if (log_entry.m_type == LogEntry::ERROR) {
                        icon = ICON_FA_TIMES_CIRCLE;
                        color = ImColor(255, 92, 92, 255);
                    }

                    utils::text_small_colored_wrapped(color, "%s %s", icon, log_entry.m_message.c_str());
                }

                // Auto scroll to down.
                if (ImGui::GetScrollY() >= ImGui::GetScrollMaxY()) {
                    ImGui::SetScrollHereY(1.0f);
                }
            }

            ImGui::EndChild();
            ImGui::PopStyleColor();
            ImGui::PopStyleVar(2);
        }
        ImGui::End();
    }
} // namespace gui
