APP_ABI := armeabi-v7a
APP_STL := c++_static
#APP_PLATFORM := android-19
NDK_TOOLCHAIN_VERSION := 4.9


APP_CPPFLAGS := -frtti  -fexceptions -DCC_ENABLE_CHIPMUNK_INTEGRATION=1 -std=c++11 -fsigned-char
APP_LDFLAGS := -latomic
