LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	gdir.c \
	gerror.c \
	giochannel.c \
	gkeyfile.c \
	gmain.c \
	gmem.c \
	goption.c \
	gslice.c \
	gslist.c \
	gstring.c \
	gstrfuncs.c \
	gtimer.c \
	giounix.c \
	gmessages.c \
	gutf8.c \
	gfileutils.c \
	gconvert.c \
	gdataset.c \
	gtestutils.c \
	ghash.c \
	glist.c \
	gthread.c \
	garray.c \
	gutils.c \
	gatomic.c \
	gprintf.c \
	gpattern.c \
	guniprop.c \
	gpoll.c \
	grand.c \
	gunidecomp.c \
	gqsort.c \
	gstdio.c

LOCAL_C_INCLUDES:= \
	$(LOCAL_PATH)/../ \
	$(LOCAL_PATH)

LOCAL_CFLAGS:= \
	-DANDROID_STUB

LOCAL_MODULE:=libglib_static

include $(BUILD_STATIC_LIBRARY)
