#pragma once
#include "ui/ImGuiWrapper.h"
#include "ui/UIView.h"

namespace ui {
    class ModMenuUI : public UIView {
    public:
        ModMenuUI(ImRect rect, const std::string &name, bool visible = true);
        ~ModMenuUI() = default;

        void draw();
    };
} // namespace ui
