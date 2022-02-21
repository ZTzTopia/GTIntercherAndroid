#pragma once
#include "ui/GUIManager.h"
#include "ui/UIView.h"

namespace ui {
    class LuaLogUI : public UIView {
    public:
        LuaLogUI(ImRect rect, const std::string &name, bool visible = true);
        ~LuaLogUI() = default;

        void draw();
    };
} // namespace ui
