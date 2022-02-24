#include <android/log.h>
#include <dlfcn.h>
#include <enet/enet.h>

#include "Hook.h"
#include "include/Dobby/dobby.h"
#include "packet/TextParse.h"
#include "packet/Decoder.h"
#include "utilities/Macros.h"

game::Game *g_game{ nullptr };
ui::UI *g_ui{ nullptr };
enet_uint16 g_port{ 65535 };

void (*BaseApp_Draw)(void *thiz);
void BaseApp_Draw_hook(void *thiz) {
    BaseApp_Draw(thiz);

    static bool initialized{ false };
    if (!initialized) {
        g_game = new game::Game{};
        g_game->init();

        auto width = KittyMemory::callFunction<float>(GTS("_Z15GetScreenSizeXfv"));
        auto height = KittyMemory::callFunction<float>(GTS("_Z15GetScreenSizeYfv"));
        g_ui = new ui::UI{ ImVec2(width, height) };
        g_ui->initialize();

        initialized = true;
    }
    else {
        if (g_ui) {
            g_ui->render();
        }

        ui::UI::handle_input();
    }
}

void (*BaseApp_SetFPSLimit)(void *thiz, float fps);
void BaseApp_SetFPSLimit_hook(void *thiz, float fps) {
    if (g_game) {
        BaseApp_SetFPSLimit(thiz, g_game->m_fpsLimit);
    }
    else {
        BaseApp_SetFPSLimit(thiz, fps);
    }
}

void (*AppOnTouch)(void *a1, void *a2, int type, float x, float y, bool multi);
void AppOnTouch_hook(void *a1, void *a2, int type, float x, float y, bool multi) {
    ImGuiIO& io = ImGui::GetIO();

    if (g_ui && (x > 0.0 || y > 0.0)) {
        g_ui->on_touch(type, multi, x, y);
    }

    if (!&io || !io.WantCaptureMouse) {
        AppOnTouch(a1, a2, type, x, y, multi);
    }
    else {
        AppOnTouch(a1, a2, 1, 0.0f, 0.0f, false);
    }
}

void (*AppOnKey)(void *, void *, int, int, int);
void AppOnKey_hook(void *a1, void *a2, int type, int keycode, int c) {
    ImGuiContext& g = *GImGui;

    // Make sure if keyboard is not opened because of imgui, we don't handle it.
    if (g.ActiveId == 0) {
        return AppOnKey(a1, a2, type, keycode, c);
    }

    switch (keycode) {
        case 66: // Enter
            c = 13;
            break;
        case 67: // Backspace
            c = 8;
            break;
        case 113: // CTRL
            return;
        default:
            break;
    }

    ImGuiIO &io = ImGui::GetIO();
    ImGuiInputTextState *state = ImGui::GetInputTextState(g.ActiveId);
    if (type == 1) {
        switch (c) {
            case 0: // No char
            case 9: // Tab
                break;
            case 8: { // Backspace
                if (state) {
                    state->ClearText();
                }

                io.InputQueueCharacters.clear();
                break;
            }
            case 13: // Enter
            case 27: // Escape
                ImGui::ClearActiveID();
                break;
            default: {
                io.AddInputCharacter(c);
                break;
            }
        }
    }

    AppOnKey(a1, a2, type, keycode, c);
}

// Because i dont know where to place this function so i put it here for now.
#include "include/proton/shared/manager/VariantDB.h"
#include "include/proton/shared/util/Variant.h"
#include "packet/Sender.h"

static ENetPeer *g_peer;
static VariantDB *g_variant_list;

void OnSpawn(VariantList *variant_list) {
    std::string on_spawn_info{ variant_list->Get(0).GetString() };
    packet::TextParse text_parse{on_spawn_info};
    if (text_parse.get_line_count() == 0) {
        return;
    }

    int net_id = text_parse.get<int>("netID", 1);
    int user_id = text_parse.get<int>("userID", 1);
    if (!net_id || !user_id) {
        return;
    }

    bool is_moderator{ false };
    if (text_parse.get<int>("invis", 1) == 1) {
        is_moderator = true;
    }

    if (text_parse.get("type", 1).find("local") != std::string::npos) {
        LOGD("[OnSpawn] Add local player");
        auto *local_player = new game::LocalPlayer{
            net_id,
            user_id,
            text_parse.get("name", 1),
            CL_Vec2i{ text_parse.get<int>("posXY", 1), text_parse.get<int>("posXY", 2) },
            is_moderator
        };
        g_game->m_world->set_local_player(local_player);
    }
    else {
        LOGD("[OnSpawn] Add remote player");
        auto *remote_player = new game::RemotePlayer{
                net_id,
                user_id,
                text_parse.get("name", 1),
                CL_Vec2i{ text_parse.get<int>("posXY", 1), text_parse.get<int>("posXY", 2) },
                is_moderator
        };
        g_game->m_world->m_remote_players.push_back(remote_player);

        if (g_game->m_player_when_join != 0) {
            if (!g_game->m_world->get_local_player()) {
                return;
            }

            if (g_game->m_world->get_owner_user_id() != g_game->m_world->get_local_player()->get_user_id()) {
                return;
            }

            std::string name = text_parse.get("name", 1);
            for (int i = 0; i < name.length(); i++) {
                if (name[i] == '`') {
                    name.erase(i, 2);
                }
            }

            switch (g_game->m_player_when_join) {
                case 1:
                    packet::sender::send_packet(NET_MESSAGE_GENERIC_TEXT, utilities::utils::string_format("action|input\n|text|/pull %s", name.c_str()), g_peer);
                    break;
                case 2:
                    packet::sender::send_packet(NET_MESSAGE_GENERIC_TEXT, utilities::utils::string_format("action|input\n|text|/kick %s", name.c_str()), g_peer);
                    break;
                case 3:
                    packet::sender::send_packet(NET_MESSAGE_GENERIC_TEXT, utilities::utils::string_format("action|input\n|text|/ban %s", name.c_str()), g_peer);
                    break;
                default:
                    break;
            }
        }
    }
}

void OnRequestWorldSelectMenu(VariantList *variant_list) {
    delete g_game->m_world;
    g_game->m_world = nullptr;
}

void OnConsoleMessage(VariantList *variant_list) {
    if (g_game->m_world) {
        std::string message{ variant_list->Get(0).GetString() };
        std::string::size_type pos = message.find("$World Locked`` by ");
        if (pos != std::string::npos) {
            std::string::size_type last_pos = message.find(" (`2ACCESS GRANTED``)");
            if (last_pos == std::string::npos) {
                last_pos = message.find("`5]``");
            }

            if (last_pos != std::string::npos) {
                LOGD("owner: %s, %d, %d", message.substr(pos + 19, last_pos - 19 - pos).c_str(), pos, last_pos);
                g_game->m_world->set_owner_name(message.substr(pos, last_pos));
            }
        }
    }
}

void Init() {
    g_variant_list = new VariantDB{};
    g_variant_list->GetFunction("OnSpawn")->sig_function = [](VariantList *variant_list) {
        if (!g_game->m_world) {
            return;
        }

        return OnSpawn(variant_list);
    };
    g_variant_list->GetFunction("OnRequestWorldSelectMenu")->sig_function = [](VariantList *variant_list) {
        if (!g_game->m_world) {
            return;
        }

        return OnRequestWorldSelectMenu(variant_list);
    };
    g_variant_list->GetFunction("OnConsoleMessage")->sig_function = [](VariantList *variant_list) {
        return OnConsoleMessage(variant_list);
    };
}

bool once{ false };

void ProcessTankUpdatePacket(GameUpdatePacket* game_update_packet) {
    if (!once) {
        Init();
        once = true;
    }

    VariantList variant_list{};

    auto *extended_data = packet::decoder::GetExtendedDataPointerFromTankPacket(game_update_packet);
    switch (static_cast<int>(game_update_packet->packetType)) {
        case PACKET_CALL_FUNCTION: {
            if (variant_list.SerializeFromMem(extended_data, static_cast<int>(game_update_packet->unk15), nullptr)) {
                LOGD("%s", variant_list.GetContentsAsDebugString().c_str());

                std::string function_name{ variant_list.Get(0).GetString() };
                variant_list.GetVariantListStartingAt(&variant_list, 1);
                g_variant_list->CallFunctionIfExists(function_name, &variant_list);
            }
            else {
                LOGD("Error reading function packet, ignoring");
            }
            break;
        }
    }
}

void (*ENetClient_ProcessPacket)(void *thiz, ENetEvent *event);
void ENetClient_ProcessPacket_hook(void *thiz, ENetEvent *event) {
    if (event->type == ENET_EVENT_TYPE_RECEIVE) {
        int type = packet::decoder::GetMessageTypeFromPacket(event->packet);
        switch (type) {
            case NET_MESSAGE_GENERIC_TEXT: {
                const char *text = packet::decoder::GetTextPointerFromPacket(event->packet);
                LOGD("Type: %d, Text: %s", type, text);
                break;
            }
            case NET_MESSAGE_GAME_MESSAGE: {
                const char *text = packet::decoder::GetTextPointerFromPacket(event->packet);
                LOGD("Type: %d, Text: %s", type, text);
                break;
            }
            case NET_MESSAGE_GAME_PACKET: {
                GameUpdatePacket *gameUpdatePacket = packet::decoder::GetStructPointerFromTankPacket(event->packet);
                if (gameUpdatePacket) {
                    LOGD("gameUpdatePacket type: %d", (int)gameUpdatePacket->packetType);
                    LOGD("unk0: %d, unk1: %d, unk2: %d, unk3: %d, unk5: %d", gameUpdatePacket->unk0, gameUpdatePacket->unk1, gameUpdatePacket->unk2, gameUpdatePacket->unk4, gameUpdatePacket->unk5);
                    LOGD("unk6: %d, unk7: %d, unk8: %d, unk9: %f, unk10: %f", gameUpdatePacket->unk6, gameUpdatePacket->unk7, gameUpdatePacket->unk8, gameUpdatePacket->unk9, gameUpdatePacket->unk10);
                    LOGD("unk11: %f, unk12: %f, unk13: %f, unk14: %d, unk15: %d", gameUpdatePacket->unk11, gameUpdatePacket->unk12, gameUpdatePacket->unk13, gameUpdatePacket->unk14, gameUpdatePacket->unk15);

                    ProcessTankUpdatePacket(gameUpdatePacket);
                }
                break;
            }
            case NET_MESSAGE_TRACK: {
                const char *text = packet::decoder::GetTextPointerFromPacket(event->packet);
                LOGD("Type: %d, Text: %s", type, text);
                packet::TextParse text_parse{ text };
                if (text_parse.get("eventName", 1).find("300_WORLD_VISIT") != std::string::npos) {
                    g_game->m_world = new game::World{};
                    g_game->m_world->set_name(text_parse.get("World_name", 1).substr(2));
                    g_game->m_world->set_owner_user_id(text_parse.get<int>("World_owner", 1));
                }
                break;
            }
            default:
                break;
        }
    }

    ENetClient_ProcessPacket(thiz, event);
}

int (*old_enet_peer_send)(ENetPeer *peer, enet_uint8 a2, ENetPacket *packet);
int enet_peer_send_hook(ENetPeer *peer, enet_uint8 a2, ENetPacket *packet) {
    g_peer = peer;
    return old_enet_peer_send(peer, a2, packet);
}

namespace game {
    namespace hook {
        void init() {
            LOGD("Initializing Hook..");

            // set Dobby logging level.
            log_set_level(0);

            // BaseApp::Draw(void)
            DobbyHook(GTS("_ZN7BaseApp4DrawEv"), (void *)BaseApp_Draw_hook, (void **)&BaseApp_Draw);

            // BaseApp::SetFPSLimit(float)
            DobbyHook(GTS("_ZN7BaseApp11SetFPSLimitEf"), (void *)BaseApp_SetFPSLimit_hook, (void **)&BaseApp_SetFPSLimit);

            // AppOnTouch(_JNIEnv *, _jobject *, int, float, float, int)
            DobbyHook(GTS("_Z10AppOnTouchP7_JNIEnvP8_jobjectiffi"), (void *)AppOnTouch_hook, (void **)&AppOnTouch);

            // AppOnKey(_JNIEnv *, _jobject *, int, int, int)
            DobbyHook(GTS("_Z8AppOnKeyP7_JNIEnvP8_jobjectiii"), (void *)AppOnKey_hook, (void **)&AppOnKey);

            // ENetClient::ProcessPacket(_ENetEvent *)
            DobbyHook(GTS("_ZN10ENetClient13ProcessPacketEP10_ENetEvent"), (void *)ENetClient_ProcessPacket_hook, (void **)&ENetClient_ProcessPacket);

            // enet_peer_send()
            DobbyHook(GTS("enet_peer_send"), (void *)enet_peer_send_hook, (void **)&old_enet_peer_send);
        }
    } // namespace hook
} // namespace game