#pragma once
#include <vector>

#include "GUIManager.h"
#include "UIView.h"

namespace ui {
    class UI : public GUIManager {
    public:
        UI(ImVec2 display_size);
        ~UI();

        void initialize();
        void render();
        static void handle_input();
        void on_touch(int type, bool multi, float x, float y);

    protected:
        void draw();

    private:
        bool m_clear_pos;

        std::vector<UIView *> m_views;
    };
} // namespace ui