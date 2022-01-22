#pragma once
#define SOL_ALL_SAFETIES_ON 1
#include <sol/sol.hpp>

namespace api {
    class LuaApi {
    public:
        LuaApi();
        ~LuaApi() {};

        void execute_script(const char *script);
        void execute_lua_file(const char *path);
        void stop();

        sol::state *get_sol_state() { return m_sol_state; }

    private:
        void init_lua();
        void init_uncategorized();

    private:
        sol::state *m_sol_state;
    }; // class Api
} // namespace api
