#pragma once
#include <string>
#include <vector>
#include <any>

#include "utilities/Utils.h"

namespace packet {
    class TextParse {
    public:
        TextParse() = default;
        TextParse(const std::string &data) {
            m_data = utilities::utils::string_tokenize(data, "\n");
            for (unsigned int i = 0; i < m_data.size(); i++) {
                utilities::utils::string_replace(m_data[i], "\r", "");
            }
        };
        ~TextParse() = default;

        std::string get(const std::string &key, int index, const std::string &token = "|") {
            if (m_data.empty()) {
                return "";
            }

            for (unsigned int i = 0; i < m_data.size(); i++) {
                if (m_data[i].empty()) {
                    continue;
                }

                std::vector<std::string> data = utilities::utils::string_tokenize(m_data[i], token);
                if (data[0] == key) {
                    // Found it.
                    return data[index];
                }
            }

            return "";
        }

        template<typename T, typename std::enable_if_t<std::is_integral_v<T>, bool> = true>
        T get(const std::string &key, int index, const std::string &token = "|") {
            return std::stoi(get(key, index, token));
        }

        template<typename T, typename std::enable_if_t<std::is_floating_point_v<T>, bool> = true>
        T get(const std::string &key, int index, const std::string &token = "|") {
            if (std::is_same_v<T, double>) {
                return std::stod(get(key, index, token));
            }
            else if (std::is_same_v<T, long double>) {
                return std::stold(get(key, index, token));
            }

            return std::stof(get(key, index, token));
        }

        void add(const std::string &key, const std::string &value, const std::string &token = "|") {
            std::string data = key + token + value;
            m_data.push_back(data);
        }

        template<typename T, typename std::enable_if_t<std::is_integral_v<T> || std::is_floating_point_v<T>, bool> = true>
        void add(const std::string &key, const T &value, const std::string &token = "|") {
            add(key, std::to_string(value), token);
        }

        void set(const std::string &key, const std::string &value, const std::string &token = "|") {
            if (m_data.empty()) {
                return;
            }

            for (unsigned int i = 0; i < m_data.size(); i++) {
                std::vector<std::string> data = utilities::utils::string_tokenize(m_data[i], token);
                if (data[0] == key) {
                    m_data[i] = data[0];
                    m_data[i] += token;
                    m_data[i] += value;
                    break;
                }
            }
        }

        template<typename T, typename std::enable_if_t<std::is_integral_v<T> || std::is_floating_point_v<T>, bool> = true>
        void set(const std::string &key, const T &value, const std::string &token = "|") {
            set_(key, std::to_string(value), token);
        }

        void get_all_raw(std::string &data) {
            if (m_data.empty()) {
                return;
            }

            std::string string{};
            for (unsigned int i = 0; i < m_data.size(); i++) {
                string += m_data[i];
                if (!m_data[i + 1].empty()) {
                    string += "\n";
                }
            }

            data = string;
        }

        size_t get_line_count() {
            return m_data.size();
        }

    private:
        std::vector<std::string> m_data;
    }; // class TextParse
} // namespace packet