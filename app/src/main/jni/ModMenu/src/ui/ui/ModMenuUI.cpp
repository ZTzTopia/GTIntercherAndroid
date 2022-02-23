#include <array>

#include "ModMenuUI.h"
#include "ui/font/IconsMaterialDesign.h"
#include "ui/UIHelper.h"
#include "game/Hook.h"
#include "include/KittyMemory/MemoryPatch.h"

namespace ui {
    ModMenuUI::ModMenuUI(ImRect rect, const std::string &name, bool visible)
        : UIView(rect, name, visible) {}

    void ModMenuUI::draw() {
        ImGuiStyle &style = ImGui::GetStyle();

        if (UIHelper::begin_window(get_rect(), get_name().c_str(), false) == 1) {
            if (ImGui::BeginTabBar("##TopTabBar")) {
                ImVec2 pos = ImGui::GetCursorScreenPos();
                ImGui::GetWindowDrawList()->AddRectFilled(ImVec2(pos.x, pos.y - 48), ImVec2(pos.x + ImGui::GetWindowSize().x, pos.y), ImColor(47, 54, 64, 255));
                if (ImGui::BeginTabItem(ICON_MD_ROCKET" Cheats")) {
                    ImGui::SetCursorPosY(ImGui::GetCursorPosY() + ImGui::GetFontSize() / 2.0f);

                    ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, style.FramePadding);
                    ImGui::PushStyleVar(ImGuiStyleVar_FrameRounding, g_ui->scale_x(8.0f));
                    if (ImGui::BeginChild("##CheatChild", ImVec2(ImGui::GetWindowSize().x, ImGui::GetWindowSize().y - (ImGui::GetCursorPosY() + ImGui::GetFontSize() / 2.0f)), false, ImGuiWindowFlags_AlwaysUseWindowPadding)) {
                        ImGui::PushFont(g_ui->get_bold_font());
                        ImGui::Text("Cheats");
                        ImGui::PopFont();

                        MemoryPatch memory_patch{};
                        for (auto& cheat_list : g_game->m_cheat_list) {
                            if (ImGui::Checkbox(cheat_list.name.c_str(), &cheat_list.state)) {
                                if (cheat_list.active != nullptr) {
                                    cheat_list.active();
                                }
                            }
                            else {
                                if (cheat_list.deactive != nullptr) {
                                    cheat_list.deactive();
                                }
                            }

                            if (cheat_list.old_state != cheat_list.state) {
                                if (!cheat_list.memory_patch_list.empty()) {
                                    for (auto& memory_patch2 : cheat_list.memory_patch_list) {
                                        cheat_list.state ? memory_patch2.Modify() : memory_patch2.Restore();
                                    }
                                }

                                cheat_list.old_state = cheat_list.state;
                            }
                        }
                    }
                    ImGui::EndChild();
                    ImGui::PopStyleVar(2);

                    ImGui::EndTabItem();
                }

                if (ImGui::BeginTabItem(ICON_MD_CODE" Executor")) {
                    ImGui::SetCursorPosY(ImGui::GetCursorPosY() + ImGui::GetFontSize() / 2.0f);

                    ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, style.FramePadding);
                    ImGui::PushStyleVar(ImGuiStyleVar_FrameRounding, g_ui->scale_x(8.0f));
                    if (ImGui::BeginChild("##ExecutorChild", ImVec2(ImGui::GetWindowSize().x, ImGui::GetWindowSize().y * (ImGui::GetCursorPosY() + ImGui::GetFontSize() / 2.0f)), false, ImGuiWindowFlags_AlwaysUseWindowPadding)) {
                        ImGui::PushFont(g_ui->get_bold_font());
                        ImGui::Text("Executor");
                        ImGui::PopFont();
                    }
                    ImGui::EndChild();
                    ImGui::PopStyleVar(2);

                    ImGui::EndTabItem();
                }

                if (ImGui::BeginTabItem(ICON_MD_MISCELLANEOUS_SERVICES" Misc")) {
                    ImGui::SetCursorPosY(ImGui::GetCursorPosY() + ImGui::GetFontSize() / 2.0f);

                    ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, style.FramePadding);
                    ImGui::PushStyleVar(ImGuiStyleVar_FrameRounding, g_ui->scale_x(8.0f));
                    if (ImGui::BeginChild("##MiscChild", ImVec2(ImGui::GetWindowSize().x, ImGui::GetWindowSize().y * (ImGui::GetCursorPosY() + ImGui::GetFontSize() / 2.0f)), false, ImGuiWindowFlags_AlwaysUseWindowPadding)) {
                        ImGui::PushFont(g_ui->get_bold_font());
                        ImGui::Text("Miscellaneous");
                        ImGui::PopFont();

                        static std::string player_when_join[4]{ "None", "Pull", "Kick", "Ban" };
                        ImGui::SliderInt("Player when join", &g_game->m_player_when_join, 0, 3, player_when_join[g_game->m_player_when_join].c_str());
                    }
                    ImGui::EndChild();
                    ImGui::PopStyleVar(2);

                    ImGui::EndTabItem();
                }
            }
            ImGui::EndTabBar();
        }
        ImGui::End();
    }
} // namespace ui
