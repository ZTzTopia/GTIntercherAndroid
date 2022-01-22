#include <future>

#include "LuaApi.h"
#include "gui/Gui.h"
#include "include/KittyMemory/KittyUtils.h"
#include "utilities/Macros.h"
#include "utilities/Utils.h"

namespace api {
    LuaApi::LuaApi() {
        m_sol_state = new sol::state();
        m_sol_state->open_libraries(sol::lib::base, sol::lib::package, sol::lib::os, sol::lib::debug);

        init_lua();
        init_uncategorized();
    }

    void LuaApi::init_lua() {
        m_sol_state->script(R"(AmEY9hS0d5SUezKolklC = 0
            debug.sethook(function(event, line)
                if AmEY9hS0d5SUezKolklC > 0 then
                    error("JRC4MQSd4qDicIUZFA2e")
                end
            end, "l")

            function d8bOfUnrRn5K4H2Fq8qn()
                AmEY9hS0d5SUezKolklC = 1
            end)");

        m_sol_state->set_function("sleep", [this](const int &time) {
            m_sol_state->script("os.execute(\"sleep 1\")");
        });

        m_sol_state->set_function("print", [](const std::string &str) {
            LOGD("[Lua] %s", str.c_str());
            g_lua_log->add(LogEntry::INFO, str);
        });
    }

    void LuaApi::init_uncategorized() {
        sol::function print = m_sol_state->get<sol::function>("print");

        m_sol_state->set_function("LogToConsole", [print](const std::string &str) {
            KittyMemory::callFunction<void>(GTS("_Z12LogToConsolePKcz"), str.c_str());
            print(str);
        });

        m_sol_state->set_function("memRead", [print](const uintptr_t &address, const int &len) {
            std::vector<uint8_t> code;
            KittyMemory::memRead(&code[0], reinterpret_cast<void *>(address), len);

            std::string result;
            KittyUtils::toHex(&code[0], code.size(), result);
            print(utilities::utils::string_format("Data: %s", result.c_str()));
        });

        m_sol_state->set_function("memWrite", [](const uintptr_t &address, const std::string &hex, const int &len) {
            std::vector<uint8_t> code;
            KittyUtils::fromHex(hex, &code[0]);
            KittyMemory::memWrite(reinterpret_cast<void *>(address), &code[0], len);
        });

        m_sol_state->set_function("patternScan", sol::overload([print](const std::string &pattern) {
            print(utilities::utils::string_format("Found: 0x%X", KittyMemory::patternScan(g_growtopia_map, pattern.c_str())));
        }, [print](const std::string &pattern, const intptr_t &offset) {
            print(utilities::utils::string_format("Found: 0x%X", KittyMemory::patternScan(g_growtopia_map, pattern.c_str(), offset)));
        }));
    }

    // Make lua not run in main c++ thread.
    // I don't know how to do this properly :).
    std::atomic<bool> hah{ false };
    std::atomic<bool> huh{ false };
    void lua_execution_thread(const char *script, bool is_file) {
        auto result = g_lua_api->get_sol_state()->script(script, sol::script_pass_on_error);
        if (!result.valid()) {
            if (!huh.load()) {
                g_lua_log->add(LogEntry::ERROR, static_cast<sol::error>(result).what());
                LOGE("[Lua] Failed to execute script (code: %d)", result.status());
            }

            g_lua_api->get_sol_state()->set("AmEY9hS0d5SUezKolklC", 0);
        }

        hah.store(false);
        huh.store(false);
    }

    void execute(const char *script, bool is_file) {
        if (hah.load()) {
            g_lua_log->add(LogEntry::WARNING, "Stop previous script before executing new one.");
            return;
        }

        hah.store(true);

        std::thread lua_thread(lua_execution_thread, script, is_file);
        lua_thread.detach();
    }

    void LuaApi::execute_script(const char *script) {
        execute(script, false);
    }

    void LuaApi::execute_lua_file(const char *file) {
        execute(file, true);
    }

    void LuaApi::stop() {
        if (hah.load()) {
            huh.store(true);
            m_sol_state->script("d8bOfUnrRn5K4H2Fq8qn()", sol::script_pass_on_error);
        }
    }
} // namespace api
