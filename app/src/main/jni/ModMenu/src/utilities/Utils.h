#pragma once
#include <vector>
#include <sstream>

namespace utilities {
    namespace utils {
        uintptr_t String2Offset(const char *c);

        std::string GenerateRandomName();
        std::string GenerateRandomNumber(size_t length);
        std::string GenerateRandomHex(size_t length, bool uppercase = false);
        std::string GenerateRandomMac();

        uint32_t HashString(const char* data, int length);
        uint32_t GetDeviceHash();
        uint32_t GetDeviceSecondaryHash();

        void string_replace(std::string &str, const std::string &from, const std::string &to);
        std::vector<std::string> string_tokenize(const std::string &str, const std::string &delimiters);
        std::string string_format(const std::string &fmt, ...);
    } // namespace utils
} // namespace utilities