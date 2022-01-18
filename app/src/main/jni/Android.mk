LOCAL_PATH_YES := $(call my-dir)
LOCAL_PATH := $(call my-dir)

# ============================================================================
# Dobby static libraries.

include $(CLEAR_VARS)
LOCAL_MODULE := libdobby
LOCAL_SRC_FILES := $(LOCAL_PATH)/libraries/$(TARGET_ARCH_ABI)/libdobby.a
include $(PREBUILT_STATIC_LIBRARY)

# ============================================================================
# Include our library.

include $(LOCAL_PATH_YES)/GrowtopiaFix/Android.mk
include $(LOCAL_PATH_YES)/ModMenu/Android.mk
