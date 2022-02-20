#include "GUIManager.h"
#include "font/IconsMaterialDesign.h"
#include "font/MaterialDesignData.h"
#include "font/NotoSansData.h"
#include "utilities/Macros.h"

#define MULT_X	0.00052083333f	// 1 / 1920
#define MULT_Y	0.00092592592f 	// 1 / 1080

namespace ui {
    GUIManager::GUIManager(ImVec2 display_size)
        : m_display_size(display_size),
        m_display_scale(ImVec2()),
        m_small_font(nullptr),
        m_bold_font(nullptr) {}

    GUIManager::~GUIManager() {
        LOGD("Destroying GUIManager");

        // Cleanup
        ImGui_ImplOpenGL3_Shutdown();
        ImGui::DestroyContext();
    }

    void GUIManager::initialize() {
        LOGD("Initializing GUIManager..");

        // Setup Dear Gui context
        IMGUI_CHECKVERSION();
        ImGui::CreateContext();

        ImGuiIO &io = ImGui::GetIO();

        // Setup display size
        io.DisplaySize = ImVec2(m_display_size.x, m_display_size.y);
        m_display_scale = ImVec2(m_display_size.x * MULT_X, m_display_size.y * MULT_Y);
        LOGD("Screen size: %f, %f", m_display_size.x, m_display_size.y);
        LOGD("Scale: %f, %f", m_display_scale.x, m_display_scale.y);

        // Setup Renderer backends
        ImGui_ImplOpenGL3_Init();

        // Material design config.
        ImFontConfig icons_config;
        icons_config.GlyphMinAdvanceX = 13.0f;
        icons_config.PixelSnapH = true;
        icons_config.MergeMode = true;
        static const ImWchar icons_ranges[] = {
                ICON_MIN_MD, ICON_MAX_MD,
                0
        };

        icons_config.GlyphOffset = ImVec2(0.0f, 6.0f);
        io.Fonts->AddFontFromMemoryCompressedBase85TTF(notosans_regular_compressed_data_base85, scale_x(38.0f));
        io.Fonts->AddFontFromMemoryCompressedBase85TTF(materialicons_regular_compressed_data_base85, scale_x(36.0f), &icons_config, icons_ranges);

        icons_config.GlyphOffset = ImVec2(0.0f, 4.5f);
        m_small_font = io.Fonts->AddFontFromMemoryCompressedBase85TTF(notosans_regular_compressed_data_base85, scale_x(32.0f));
        io.Fonts->AddFontFromMemoryCompressedBase85TTF(materialicons_regular_compressed_data_base85, scale_x(28.0f), &icons_config, icons_ranges);

        icons_config.GlyphOffset = ImVec2(0.0f, 6.0f);
        m_bold_font = io.Fonts->AddFontFromMemoryCompressedBase85TTF(notosans_bold_compressed_data_base85, scale_x(38.0f));
        io.Fonts->AddFontFromMemoryCompressedBase85TTF(materialicons_regular_compressed_data_base85, scale_x(36.0f), &icons_config, icons_ranges);

        ImGuiStyle &style = ImGui::GetStyle();
        ImGui::StyleColorsClassic();

        style.WindowPadding = ImVec2(0.0f, 0.0f);
        style.WindowBorderSize = 0.0f;
        style.WindowRounding = 8.0f;

        style.ScrollbarSize = scale_y(35.0f);

        style.FramePadding = ImVec2(scale_x(38.0f), 0.0f);

        style.FrameBorderSize = 0.0f;
        style.ChildBorderSize = 1.0f;

        style.ItemSpacing = ImVec2(scale_x(12.0f), scale_y(12.0f));

        // Colors
        style.Colors[ImGuiCol_WindowBg] = ImVec4(53.0f / 255.0f, 59.0f / 255.0f, 72.0f / 255.0f, 1.0f);

        style.Colors[ImGuiCol_ScrollbarBg] = ImVec4(47.0f / 255.0f, 54.0f / 255.0f, 64.0f / 255.0f, 1.0f);
        style.Colors[ImGuiCol_ScrollbarGrab] = ImVec4(156.0f / 255.0f, 136.0f / 255.0f, 255.0f / 255.0f, 0.5f);
        style.Colors[ImGuiCol_ScrollbarGrabHovered] = ImVec4(156.0f / 255.0f, 136.0f / 255.0f, 255.0f / 255.0f, 1.0f);
        style.Colors[ImGuiCol_ScrollbarGrabActive] = ImVec4(156.0f / 255.0f, 136.0f / 255.0f, 255.0f / 255.0f, 1.0f);

        style.Colors[ImGuiCol_FrameBg] = ImVec4(45.0f / 255.0f, 49.0f / 255.0f, 58.0f / 255.0f, 1.0f);
        style.Colors[ImGuiCol_FrameBgActive] = ImVec4(156.0f / 255.0f, 136.0f / 255.0f, 255.0f / 255.0f, 1.0f);

        // style.Colors[ImGuiCol_Button] = ImVec4(53.0f / 255.0f, 59.0f / 255.0f, 72.0f / 255.0f, 1.0f);
        style.Colors[ImGuiCol_Button] = ImVec4(45.0f / 255.0f, 49.0f / 255.0f, 58.0f / 255.0f, 1.0f);
        style.Colors[ImGuiCol_ButtonHovered] = ImVec4(156.0f / 255.0f, 136.0f / 255.0f, 255.0f / 255.0f, 1.0f);
        style.Colors[ImGuiCol_ButtonActive] = ImVec4(156.0f / 255.0f, 136.0f / 255.0f, 255.0f / 255.0f, 1.0f);

        style.Colors[ImGuiCol_Border] = ImVec4(156.0f / 255.0f, 136.0f / 255.0f, 255.0f / 255.0f, 1.0f);
        style.Colors[ImGuiCol_CheckMark] = ImVec4(1.0f, 1.0f, 1.0f, 1.0f);
    }

    void GUIManager::render() {
        // Start the Dear Gui frame
        ImGui_ImplOpenGL3_NewFrame();
        ImGui::NewFrame();

        draw();

        // Rendering
        ImGuiIO &io = ImGui::GetIO();
        ImGui::EndFrame();
        ImGui::Render();
        glViewport(0, 0, (int)io.DisplaySize.x, (int)io.DisplaySize.y);
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
    }
} // namespace ui