cmake_minimum_required(VERSION 3.18..3.24)
project(GrowtopiaFix LANGUAGES CXX)

file(GLOB INCLUDE_FILES
    game/*.h)

file(GLOB SOURCE_FILES
    *.cpp
    game/*.cpp)

add_library(${PROJECT_NAME} SHARED
    ${INCLUDE_FILES}
    ${SOURCE_FILES})

target_link_libraries(${PROJECT_NAME}
    android
    log
    dobby)
