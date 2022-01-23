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
        // Script to use for stopping the script. (maybe not needed anymore, who knows?)
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
            m_sol_state->script(utilities::utils::string_format("os.execute(\"sleep %d\")", time));
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
            print(utilities::utils::string_format("Data: %s", KittyMemory::read2HexStr(reinterpret_cast<void *>(address), len).c_str()));
        });

        m_sol_state->set_function("memWrite", [print](const uintptr_t &address, const std::string &hex, const int &len) {
            std::vector<uint8_t> code;
            code.resize(len);

            KittyUtils::fromHex(hex, &code[0]);
            LOGD("[Lua] Writing %d bytes to 0x%x", code.size(), address);
            if (KittyMemory::memWrite(reinterpret_cast<void *>(address), &code[0], len) != KittyMemory::SUCCESS) {
                LOGE("[Lua] Failed to write memory");
                print("Failed to write memory");
            }
        });

        m_sol_state->set_function("patternScan", sol::overload([print](const std::string &pattern) {
            print(utilities::utils::string_format("Pattern address: 0x%X", KittyMemory::patternScan(g_growtopia_map, pattern.c_str())));
        }, [print](const std::string &pattern, const intptr_t &offset) {
            print(utilities::utils::string_format("Pattern address: 0x%X", KittyMemory::patternScan(g_growtopia_map, pattern.c_str(), offset)));
        }));

        m_sol_state->set_function("getAddressFromSymbol", sol::overload([print](const std::string &symbol) {
            print(utilities::utils::string_format("Symbol address: 0x%X", reinterpret_cast<uintptr_t>(dlsym(nullptr, symbol.c_str()))));
        }));

        m_sol_state->set_function("compareData", sol::overload([print](const uintptr_t &address, const std::string &pattern) {
            print(utilities::utils::string_format("Same data: %s", KittyMemory::compareData(reinterpret_cast<char *>(address), pattern.c_str()) ? "true" : "false"));
        }));
    }

    // Make lua not run in main c++ thread.
    // I don't know how to do this properly :).
    static std::atomic<bool> g_script_running{ false };
    static std::atomic<bool> g_stop_script{ false };
    void lua_execution_thread(LuaApi *lua_api, const char *script, bool is_file) {
        std::future<void> future = std::async(std::launch::async, [lua_api, script, is_file]() {
            if (is_file) {
                auto result = lua_api->get_sol_state()->script_file(script, sol::script_pass_on_error);
                if (!result.valid()) {
                    if (!g_stop_script.load()) {
                        g_lua_log->add(LogEntry::ERROR, static_cast<sol::error>(result).what());
                        LOGE("[Lua] Failed to execute script file: %s (code: %d)", script, result.status());
                    }
                }
            }
            else {
                auto result = lua_api->get_sol_state()->script(script, sol::script_pass_on_error);
                if (!result.valid()) {
                    if (!g_stop_script.load()) {
                        g_lua_log->add(LogEntry::ERROR, static_cast<sol::error>(result).what());
                        LOGE("[Lua] Failed to execute script (code: %d)", result.status());
                    }
                }
            }

            g_script_running.store(false);
        });

        while (g_script_running) {
            if (g_stop_script) {
                break;
            }

            std::this_thread::sleep_for(std::chrono::milliseconds(100));
        }

        g_script_running.store(false);
    }

    void LuaApi::execute(const char *script, bool is_file) {
        if (g_script_running.load()) {
            g_lua_log->add(LogEntry::WARNING, "Stop previous script before executing new one.");
            return;
        }

        g_script_running.store(true);
        g_stop_script.store(false);

        m_sol_state->set("AmEY9hS0d5SUezKolklC", 0);

        std::thread lua_thread(lua_execution_thread, this, script, is_file);
        lua_thread.detach();
    }

    void LuaApi::execute_script(const char *script) {
        execute(script, false);
    }

    void LuaApi::execute_lua_file(const char *file) {
        execute(file, true);
    }

    void LuaApi::stop() {
        if (g_script_running.load()) {
            g_stop_script.store(true);
            m_sol_state->script("d8bOfUnrRn5K4H2Fq8qn()", sol::script_pass_on_error);
        }
    }
} // namespace api