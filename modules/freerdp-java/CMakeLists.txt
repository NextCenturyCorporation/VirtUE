cmake_minimum_required(VERSION 3.5.1)

project(freerdp-java Java CXX)
set(CMAKE_CXX_FLAGS "-std=c++14")

find_package(SWIG REQUIRED)
find_package(Java 1.8 REQUIRED)
find_package(JNI REQUIRED)
find_package(FreeRDP REQUIRED)
find_package(WinPR REQUIRED) # required by FreeRDP

include(UseJava)
include(UseSWIG)
include_directories(SYSTEM
	${FreeRDP_INCLUDE_DIR}
	${WinPR_INCLUDE_DIR}
	${JNI_INCLUDE_DIRS}
	)

set(CMAKE_SWIG_OUTDIR build/java/com/nextcentury/savior/freerdp)

set_source_files_properties(src/main/swig/freerdp.i PROPERTIES
													CPLUSPLUS ON
													)
set(CMAKE_SWIG_FLAGS -package com.nextcentury.savior.freerdp)
include_directories(src/main/cpp)

swig_add_module(
	jfreerdp
	java
	src/main/swig/freerdp.i
	src/main/cpp/FreeRDP.h
	src/main/cpp/FreeRDP.cpp
	)
#set(SWIG_MODULE_jfreerdp_EXTRA_DEPS src/main/cpp/FreeRDP.cpp)

# set(SWIG_JAVA_SOURCE_LIST "build/swig-java-files")
# add_custom_command(
# 	OUTPUT ${SWIG_JAVA_SOURCE_LIST}
# 	COMMAND file(GLOB FREERDP_JAVA_SOURCES ${CMAKE_CURRENT_BINARY_DIR}/${CMAKE_SWIG_OUTDIR}/*.java) 
# 
# 
# add_jar(freerdp-java
# 	SOURCES @${SWIG_JAVA_SOURCE_LIST}
# 	OUTPUT_DIR ${CMAKE_CURRENT_BINARY_DIR}/build
# 	)
# add_dependencies(freerdp-java jfreerdp)
