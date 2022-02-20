#pragma once
#include <string>

namespace ui {
    class UIView {
    public:
        UIView(ImRect rect, std::string name, bool visible = true)
            : m_rect(rect),
            m_name(name),
            m_visible(visible) {}
        ~UIView() = default;

        virtual void draw() = 0;

        ImRect get_rect() const { return m_rect; }
        std::string get_name() const { return m_name; }
        bool is_visible() const { return m_visible; }

    private:
        ImRect m_rect;
        std::string m_name;
        bool m_visible;
    };
}
