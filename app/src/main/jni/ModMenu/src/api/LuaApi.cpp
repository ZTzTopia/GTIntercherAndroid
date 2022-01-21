#include "LuaApi.h"
#include "utilities/Macros.h"

namespace api {
    LuaApi::LuaApi()
        : m_lua_error("")
    {
        m_lua = new sol::state();
        m_lua->open_libraries(sol::lib::base, sol::lib::package);

        init_uncategorized();
    }

    void LuaApi::init_uncategorized() {
        m_lua->set_function("print", [](const std::string &str) {
            LOGD("[Lua] %s", str.c_str());
        });

        // hmm
    }

    sol::call_status LuaApi::execute_script(const char *script) {
        auto result = m_lua->script(script, sol::script_pass_on_error);
        if (!result.valid()) {
            m_lua_error = static_cast<sol::error>(result).what();
            LOGE("[Lua] Failed to execute script. (code: %d)", result.status());
            return result.status();
        }

        return sol::call_status::ok;
    }

    sol::call_status LuaApi::execute_lua_file(const char *file) {
        auto result = m_lua->script_file(file, sol::script_pass_on_error);
        if (!result.valid()) {
            m_lua_error = static_cast<sol::error>(result).what();
            LOGE("[Lua] Failed to execute script file: %s (code: %d)", file, result.status());
            return result.status();
        }

        return sol::call_status::ok;
    }

    int LuaApi::stop() {
        return -1;
    }

    std::string LuaApi::get_last_error() {
        return m_lua_error;
    }
} // namespace api