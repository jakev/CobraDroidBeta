LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	op_alloc_counter.c \
	op_config.c \
	op_cpu_type.c \
	op_events.c \
	op_get_interface.c \
	op_mangle.c \
	op_parse_event.c \
	op_xml_events.c \
	op_xml_out.c

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/.. \
	$(LOCAL_PATH)/../libutil

LOCAL_MODULE := libop

include $(BUILD_STATIC_LIBRARY)
