#include "ModMenuUI.h"
#include "ui/UIHelper.h"
#include "game/Hook.h"
#include "include/KittyMemory/MemoryPatch.h"

namespace ui {
    ModMenuUI::ModMenuUI(ImRect rect, std::string name, bool visible)
        : UIView(rect, std::move(name), visible) {}

    void ModMenuUI::draw() {
        static bool collapsed{ false };

        ImGui::SetNextWindowPos(ImVec2(get_rect().Min.x, get_rect().Min.y), ImGuiCond_Once);
        ImGui::SetNextWindowSize(ImVec2(get_rect().Max.x, collapsed ? 19 + ImGui::GetFontSize() * 1.5f : get_rect().Max.y));

        ImGui::Begin((get_name() + "##ModMenu").c_str(), nullptr, ImGuiWindowFlags_NoDecoration);
        {
            uint8_t ret = UIHelper::create_title_bar(get_name(), false);
            if (ret == -1) {
                ImGui::End();
                return;
            }
            else if (ret == 0) {
                ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, ImGui::GetStyle().FramePadding);
                ImGui::PushStyleVar(ImGuiStyleVar_FrameRounding, g_ui->scale_x(8.0f));
                ImGui::BeginChild("##CheatChild", ImVec2(ImGui::GetWindowSize().x, ImGui::GetWindowSize().y * 0.71f), false, ImGuiWindowFlags_AlwaysUseWindowPadding);

                static char search_text_[128];
                ImGui::InputText("Search", search_text_, 128, ImGuiInputTextFlags_Password);

                ImGui::EndChild();
                ImGui::PopStyleVar(2);
            }

            collapsed = ret == 1;
        }
        ImGui::End();
    }
} // namespace ui
