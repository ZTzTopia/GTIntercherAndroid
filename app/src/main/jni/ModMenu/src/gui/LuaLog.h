#pragma once
#include "string"
#include "vector"

struct LogEntry {
    enum eLogEntryType {
        NONE,
        INFO,
        WARNING,
        ERROR
    };

    eLogEntryType m_type;
    std::string m_message;
};

namespace gui {
    class LuaLog {
    public:
        LuaLog();
        ~LuaLog() {};

        void add(LogEntry::eLogEntryType type, const std::string message) {
            if (message.empty()) {
                return;
            }

            LogEntry entry;
            entry.m_type = type;
            entry.m_message = message;

            m_log_entry.push_back(entry);
            m_visible = true;
        }
        void clear() { m_log_entry.clear(); m_visible = false; }
        void toggle() { m_visible = !m_visible; }
        void render();

    private:
        bool m_visible;
        std::vector<LogEntry> m_log_entry;
    };
} // namespace gui
