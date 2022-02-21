#pragma once
#include "UI.h"
#include "font/IconsMaterialDesign.h"
#include "ui/LuaLogUI.h"
#include "ui/ModMenuUI.h"
#include "utilities/JavaWrapper.h"
#include "utilities/Macros.h"

namespace ui {
    UI::UI(ImVec2 display_size)
        : GUIManager(display_size),
        m_clear_pos(true) {
        m_views.clear();
    }

    UI::~UI() {
        for (auto &view : m_views) {
            delete view;
        }
    }

    void UI::initialize() {
        LOGD("Initializing UI..");

        GUIManager::initialize();

        m_views.push_back(new ModMenuUI(ImRect(64, 64, get_display_size().x / 2.0f, get_display_size().y / 1.5f), "ModMenu"));
        m_views.push_back(new LuaLogUI(ImRect(64, 64, get_display_size().x / 2.5f, get_display_size().y / 2.0f), "LuaLog", false));
    }

    void UI::render() {
        GUIManager::render();

        if (m_clear_pos) {
            ImGuiIO &io = ImGui::GetIO();
            io.MousePos = ImVec2(-FLT_MAX, -FLT_MAX);
            m_clear_pos = false;
        }
    }

    void UI::draw() {
        for (auto &view : m_views) {
            if (!view->is_visible()) {
                continue;
            }

            view->draw();
        }
    }

    void UI::handle_input() {
        ImGuiContext& g = *GImGui;
        ImGuiIO &io = ImGui::GetIO();

        static bool last_want_text_input{ false };
        static ImGuiID last_active_id{ 0 };

        if (io.WantTextInput && !last_want_text_input) {
            ImGuiInputTextState *state = ImGui::GetInputTextState(g.ActiveId);
            if (state) {
                const bool is_password = (state->InitFlags & ImGuiInputTextFlags_Password) != 0;
                if ((state->InitFlags & ImGuiInputTextFlags_ReadOnly) != 0) {
                    return;
                }

                utilities::java_wrapper::show_soft_input(true, state->InitialTextA.Data, is_password, state->BufCapacityA - 1);
            }
        }

        last_want_text_input = io.WantTextInput;
        if (last_want_text_input && g.ActiveId != last_active_id) {
            utilities::java_wrapper::show_soft_input(false, "", false);

            // If another item is focused and want text input,
            // we need to reopen the soft input.
            if (!io.WantTextInput || g.ActiveId != 0) {
                last_want_text_input = false;
            }
        }

        last_active_id = g.ActiveId;

        // Set the cursor position always end of text.
        ImGuiInputTextState *state = ImGui::GetInputTextState(g.ActiveId);
        if (state) {
            state->SelectAll();
            if (state->HasSelection()) {
                state->ClearSelection();
            }
        }
    }

    void UI::on_touch(int type, bool multi, float x, float y) {
        ImGuiIO &io = ImGui::GetIO();
        switch (type) {
            case 0:
                io.MousePos = ImVec2(x, y);
                io.MouseDown[0] = true;
                break;
            case 1:
                io.MouseDown[0] = false;
                m_clear_pos = true;
                break;
            case 2:
                io.MousePos = ImVec2(x, y);
                break;
            default:
                break;
        }
    }
} // namespace ui