#pragma once
#include <sol/sol.hpp>

namespace api {
    class LuaApi {
    public:
        LuaApi();
        ~LuaApi() {};

        sol::call_status execute_script(const char *script);
        sol::call_status execute_lua_file(const char *path);
        int stop();

        std::string get_last_error();
        void clear_last_error() { m_lua_error.clear(); }

    private:
        void init_uncategorized();

    private:
        sol::state *m_lua;
        std::string m_lua_error;
    }; // class Api
} // namespace api
