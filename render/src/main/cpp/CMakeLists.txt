# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("glnative-render")

include_directories(
        include
        include/opencv_3_0_0

        utils
        utils/graphics
        utils/log
        utils/time

        model
        sample
)

#自定义搜索规则
file(GLOB src-files
        ${CMAKE_SOURCE_DIR}/*.cpp

        ${CMAKE_SOURCE_DIR}/*/*.cpp

        ${CMAKE_SOURCE_DIR}/*/*/*.cpp

        ${CMAKE_SOURCE_DIR}/*/*/*/*.cpp

        ${CMAKE_SOURCE_DIR}/*/*/*/*/*.cpp

        ${CMAKE_SOURCE_DIR}/*/*/*/*/*/*.cpp

        ${CMAKE_SOURCE_DIR}/*/*/*/*/*/*/*.cpp
        )

set(thirdPartyLibDir "${CMAKE_SOURCE_DIR}/3rdParty")
# 导入assimp
add_library(assimp SHARED IMPORTED)
set_target_properties(assimp
        PROPERTIES IMPORTED_LOCATION
        ${thirdPartyLibDir}/${CMAKE_ANDROID_ARCH_ABI}/libassimp.so)

# 导入opencv
add_library(opencv_java3 SHARED IMPORTED)
set_target_properties(opencv_java3
        PROPERTIES IMPORTED_LOCATION
        ${thirdPartyLibDir}/${CMAKE_ANDROID_ARCH_ABI}/libopencv_java3.so)

# 把第三方库集中起来
set(thirdPartyLibs
        assimp
        opencv_java3
        )

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        glnative-render

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${src-files})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        glnative-render

        # 添加链接库
        android
        GLESv3
        EGL

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${thirdPartyLibs})