LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := nativecommand
LOCAL_SRC_FILES := com_xd_adhocroute_nativehelper_NativeTask.c
LOCAL_LDLIBS += -llog
include $(BUILD_SHARED_LIBRARY)
