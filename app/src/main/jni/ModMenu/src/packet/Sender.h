#pragma once
#include <iostream>
#include <string>
#include <enet/enet.h>

#include "Common.h"
#include "utilities/Macros.h"

namespace packet {
    namespace sender {
        inline void SendPacket(eNetMessageType messageType, const std::string& data, ENetPeer *peer) {
            if (peer) {
                ENetPacket* packet = enet_packet_create(0, data.length() + 5, 1);
                *(eNetMessageType*)packet->data = messageType;
                memcpy(packet->data + 4, data.c_str(), data.length());

                peer->host->usingNewPacket = 1;

                packet->freeCallback = [](ENetPacket *packet) {
                    LOGD("Packet send successfully.");
                };

                int ret;
                if (ret = enet_peer_send(peer, 0, packet) != 0) {
                    enet_packet_destroy(packet);
                }

                LOGD("ret: %d", ret);
            }
            else {
                LOGD("Bad peer");
            }
        }

        inline void SendPacketPacket(ENetPacket* packet, ENetPeer *peer) {
            if (peer) {
                ENetPacket* packetTwo = enet_packet_create(0, packet->dataLength, packet->flags);
                memcpy(packetTwo->data, packet->data, packet->dataLength);
                if (enet_peer_send(peer, 0, packetTwo) != 0) {
                    enet_packet_destroy(packetTwo);
                }
                return;
            }
        }

        inline void SendPacketRaw(eNetMessageType messageType, GameUpdatePacket* gameUpdatePacket, size_t length, unsigned char* extendedData, enet_uint32 flags, ENetPeer *peer) {
            if (peer) {
                if (length > 0xf4240u) {
                    LOGD("Huge Packet Size %d", length);
                    return;
                }

                ENetPacket* packet = enet_packet_create(0, length + 5, flags);
                *(eNetMessageType*)packet->data = messageType;
                memcpy(packet->data + 4, gameUpdatePacket, length);

                if (messageType == NET_MESSAGE_GAME_PACKET && (gameUpdatePacket->unk6 & 8) != 0) {
                    memcpy(packet->data + length + 4, extendedData, gameUpdatePacket->unk15);
                    *(uint32_t*)(packet->data + length + gameUpdatePacket->unk15 + 4) = 0x21402e40;
                }
                else {
                    *(uint32_t*)(packet->data + length + 4) = 0x21402e40;
                }

                if (enet_peer_send(peer, 0, packet) != 0) {
                    enet_packet_destroy(packet);
                }
            }
        }
    } // namespace sender
} // namespace packet