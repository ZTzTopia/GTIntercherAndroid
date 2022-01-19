LOCAL_PATH := $(call my-dir)

# ============================================================================
# Dobby static libraries.

include $(CLEAR_VARS)
LOCAL_MODULE := libdobby
LOCAL_SRC_FILES := $(LOCAL_PATH)/src/libraries/$(TARGET_ARCH_ABI)/libdobby.a
include $(PREBUILT_STATIC_LIBRARY)

# ============================================================================
# Build the GrowtopiaFix library.

include $(CLEAR_VARS)
LOCAL_MODULE := GrowtopiaFix
LOCAL_ARM_MODE := arm

LOCAL_STATIC_LIBRARIES := libdobby

LOCAL_CFLAGS := -fvisibility=hidden
LOCAL_CPPFLAGS := -w -s -fvisibility=hidden -pthread -Wall -O3 -std=c++17

FILE_LIST := $(wildcard $(LOCAL_PATH)/src/*.c*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/game/*.c*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/include/KittyMemory/*.c*)

LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_LDLIBS := -llog -landroid

include $(BUILD_SHARED_LIBRARY)
