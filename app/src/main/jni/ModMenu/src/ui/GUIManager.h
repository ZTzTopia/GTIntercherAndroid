#pragma once
#include <GLES2/gl2.h>
#include <imgui.h>
#include <imgui_internal.h>
#include <backends/imgui_impl_opengl3.h>

namespace ui {
    class GUIManager {
    public:
        GUIManager(ImVec2 display_size);
        ~GUIManager();

        virtual void initialize();
        virtual void render();

        ImVec2 get_display_size() { return m_display_size; }

        float scale_x(float x) { return x * m_display_scale.x; }
        float scale_y(float y) { return y * m_display_scale.y; }

        ImFont *get_small_font() { return m_small_font; }
        ImFont *get_bold_font() { return m_bold_font; }

    protected:
        virtual void draw() = 0;

    private:
        ImVec2 m_display_size;
        ImVec2 m_display_scale;

        ImFont *m_small_font;
        ImFont *m_bold_font;
    };
} // namespace ui