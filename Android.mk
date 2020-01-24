LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	src/me/zhengnian/getlayoutservice/IGetLayout.aidl
	
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res

LOCAL_PACKAGE_NAME := GetLayoutService

LOCAL_CERTIFICATE := shared

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))