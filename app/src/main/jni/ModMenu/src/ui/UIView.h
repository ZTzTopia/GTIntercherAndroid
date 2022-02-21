#pragma once
#include <string>

namespace ui {
    class UIView {
    public:
        UIView(ImRect rect, const std::string &name, bool visible = true)
            : m_rect(rect),
            m_name(name),
            m_visible(visible) {}
        ~UIView() = default;

        virtual void draw() = 0;

        ImRect get_rect() { return m_rect; }
        std::string get_name() { return m_name; }
        void set_visible(bool visible) { m_visible = visible; }
        bool is_visible() { return m_visible; }

    private:
        ImRect m_rect;
        std::string m_name;
        bool m_visible;
    };
}
