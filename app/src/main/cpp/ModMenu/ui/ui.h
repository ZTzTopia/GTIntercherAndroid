#pragma once
#include <imgui.h>

#include "imgui_wrapper.h"

namespace ui {
class Ui : ImGuiWrapper {
public:
    Ui(ImVec2 display_size);
    ~Ui() = default;

    void render() override;
    void draw() override;

private:
    bool m_clear_pos;
};
} // ui
