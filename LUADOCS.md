# Lua Documentation
Documentation for lua api used to make own cheat.

## 📜 Enumerations
```cpp
enum eNetMessageType {
    NET_MESSAGE_UNKNOWN = 0,
    NET_MESSAGE_SERVER_HELLO,
    NET_MESSAGE_GENERIC_TEXT,
    NET_MESSAGE_GAME_MESSAGE,
    NET_MESSAGE_GAME_PACKET,
    NET_MESSAGE_ERROR,
    NET_MESSAGE_TRACK,
    NET_MESSAGE_CLIENT_LOG_REQUEST,
    NET_MESSAGE_CLIENT_LOG_RESPONSE,
    NET_MESSAGE_GENERIC_TEXT2 = 0x69,
    NET_MESSAGE_GENERIC_TEXT3 = 0x48,
    NET_MESSAGE_CLIENT_LOG_GETASYNC = 0x24,
    NET_MESSAGE_CLIENT_TRACK_RESPONSE = 0x83,
    NET_MESSAGE_CLIENT_SYSTEM_RESPONSE = 0x44
};
```

## 📜 Structure
```cpp
struct GameUpdatePacket {
    uint8_t packetType; // 0
    uint8_t unk0; // 1
    uint8_t unk1; // 2
    uint8_t unk2; // 3
    uint32_t unk4; // 4
    uint32_t unk5; // 8
    uint32_t unk6; // 12
    uint32_t unk7; // 16
    uint32_t unk8; // 20
    float unk9; // 24
    float unk10; // 28
    float unk11; // 32
    float unk12; // 36
    float unk13; // 40
    uint32_t unk14; // 44
    uint32_t unk15; // 48
    uint32_t dataExtendedLength; // 52
    uint32_t dataExtended; // 56
};
```

## 📜 Functions

To print message to console.

Paramenters:
  - message: message
```lua
LogToConsole(message);
```

Send a packet to server.

Paramenters:
  - type: packet type (See type eNetMessageType enumeration)
  - string: packet string

Return:
  - 0 on success
  - < 0 on failure
```lua
SendPacket(type, string);
```
