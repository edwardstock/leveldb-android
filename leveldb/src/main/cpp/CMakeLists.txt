project(leveldb_jni
        LANGUAGES CXX)
cmake_minimum_required(VERSION 3.10)

set(CMAKE_CXX_STANDARD 14)

set(LEVELDB_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LEVELDB_BUILD_BENCHMARKS OFF CACHE BOOL "" FORCE)
set(LEVELDB_INSTALL OFF CACHE BOOL "" FORCE)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -fexceptions -fno-rtti -fPIC -Wno-unused-variable -pedantic -Wno-unused-parameter -Wno-format -Wno-shift-count-overflow")
set(CMAKE_CXX_FLAGS_DEBUG "-g -O0")
set(CMAKE_CXX_FLAGS_RELEASE "-O3")

if (ANDROID_PLATFORM)
    add_definitions(-D__ANDROID__)
endif ()

add_subdirectory(leveldb)


set(JNI_SOURCES
        ${CMAKE_CURRENT_SOURCE_DIR}/binding/com_github_hf_leveldb_implementation_NativeIterator.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/binding/com_github_hf_leveldb_implementation_NativeIterator.h
        ${CMAKE_CURRENT_SOURCE_DIR}/binding/com_github_hf_leveldb_implementation_NativeLevelDB.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/binding/com_github_hf_leveldb_implementation_NativeLevelDB.h
        ${CMAKE_CURRENT_SOURCE_DIR}/binding/com_github_hf_leveldb_implementation_NativeWriteBatch.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/binding/com_github_hf_leveldb_implementation_NativeWriteBatch.h
        )

add_library(${PROJECT_NAME} SHARED ${JNI_SOURCES})
target_link_libraries(${PROJECT_NAME} leveldb log dl)