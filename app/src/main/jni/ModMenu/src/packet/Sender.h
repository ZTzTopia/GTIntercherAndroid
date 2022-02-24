#pragma once
#include <iostream>
#include <string>
#include <enet/enet.h>

#include "Common.h"
#include "utilities/Macros.h"

namespace packet {
    namespace sender {
        inline void send_packet(eNetMessageType type, const std::string &data, ENetPeer *peer) {
            if (peer) {
                ENetPacket* packet = enet_packet_create(0, data.length() + 5, 1);
                *(eNetMessageType*)packet->data = type;
                memcpy(packet->data + 4, data.c_str(), data.length());

                packet->pad = 0;
                peer->host->usingNewPacket = 1;

                packet->freeCallback = [](ENetPacket *packet) {
                    LOGD("Packet send successfully.");
                };

                int ret;
                if (ret = enet_peer_send(peer, 0, packet) != 0) {
                    LOGD("Failed to send packet: %d", ret);
                    enet_packet_destroy(packet);
                }
            }
            else {
                LOGD("Bad peer");
            }
        }

        inline void send_packet_packet(ENetPacket* packet, ENetPeer *peer) {
            if (peer) {
                ENetPacket* packetTwo = enet_packet_create(0, packet->dataLength, packet->flags);
                memcpy(packetTwo->data, packet->data, packet->dataLength);

                packetTwo->pad = packet->pad;
                peer->host->usingNewPacket = 1;

                packetTwo->freeCallback = [](ENetPacket *packet) {
                    LOGD("Packet send successfully.");
                };

                int ret;
                if (ret = enet_peer_send(peer, 0, packetTwo) != 0) {
                    LOGD("Failed to send packet: %d", ret);
                    enet_packet_destroy(packetTwo);
                }
                return;
            }
        }

        inline void send_packet_raw(eNetMessageType messageType, GameUpdatePacket* gameUpdatePacket, size_t length, unsigned char* extendedData, enet_uint32 flags, ENetPeer *peer) {
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

                packet->pad = 0;
                peer->host->usingNewPacket = 1;

                packet->freeCallback = [](ENetPacket *packet) {
                    LOGD("Packet send successfully.");
                };

                int ret;
                if (ret = enet_peer_send(peer, 0, packet) != 0) {
                    LOGD("Failed to send packet: %d", ret);
                    enet_packet_destroy(packet);
                }
            }
        }
    } // namespace sender
} // namespace packet