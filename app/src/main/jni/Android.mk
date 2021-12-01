LOCAL_PATH := $(call my-dir)

# Build the GrowtopiaFix library.
include $(CLEAR_VARS)
LOCAL_MODULE := GrowtopiaFix
LOCAL_ARM_MODE := arm

LOCAL_CFLAGS := -fvisibility=hidden
LOCAL_CPPFLAGS := -w -s -fvisibility=hidden -pthread -Wall -O3 -std=c++17

FILE_LIST := $(wildcard $(LOCAL_PATH)/src/*.c*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/game/*.c*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/include/Substrate/*.c*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/include/And64InlineHook/*.c*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/include/KittyMemory/*.c*)

LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_LDLIBS := -llog -landroid -lEGL -lGLESv2

include $(BUILD_SHARED_LIBRARY)
