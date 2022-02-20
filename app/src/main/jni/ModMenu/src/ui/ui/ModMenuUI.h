#pragma once
#include "ui/GUIManager.h"
#include "ui/UIView.h"

namespace ui {
    class ModMenuUI : public UIView {
    public:
        ModMenuUI(ImRect rect, std::string name, bool visible = true);
        ~ModMenuUI() = default;

        void draw();
    };
} // namespace ui
