# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_VERBOSE_MAKEFILE on)

set(cpp_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp)
set(lib_ffmpeg_sdk_DIR ${cpp_DIR}/libffmpegsdk)


add_library(
             NativeVideoMeeting
             SHARED
             src/main/cpp/HandleVideoFrame.c
             )

add_library(libavcodec SHARED IMPORTED )
set_target_properties(libavcodec PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libavcodec.so
                  )

add_library(libavdevice SHARED IMPORTED )
set_target_properties(libavdevice PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libavdevice.so
                  )

add_library(libavfilter SHARED IMPORTED )
set_target_properties(libavfilter PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libavfilter.so
                  )


add_library(libavformat SHARED IMPORTED )
set_target_properties(libavformat PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libavformat.so
                  )

add_library(libavresample SHARED IMPORTED )
set_target_properties(libavresample PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libavresample.so
                  )

add_library(libavutil SHARED IMPORTED )
set_target_properties(libavutil PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libavutil.so
                  )

add_library(libffmpegjni SHARED IMPORTED )
set_target_properties(libffmpegjni PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libffmpegjni.so
                  )

add_library(libswresample SHARED IMPORTED )
set_target_properties(libswresample PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libswresample.so
                  )

add_library(libswscale SHARED IMPORTED )
set_target_properties(libswscale PROPERTIES IMPORTED_LOCATION
                  ${lib_ffmpeg_sdk_DIR}/${ANDROID_ABI}/libswscale.so
                  )

include_directories(${cpp_DIR}/include)

target_link_libraries( NativeVideoMeeting
                       libavcodec
                       libavdevice
                       libavfilter
                       libavformat
                       libavresample
                       libavutil
                       libffmpegjni
                       libswresample
                       libswscale
                       log android z GLESv2 EGL)
