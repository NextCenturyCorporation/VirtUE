cmake_minimum_required(VERSION 3.5.1)

if(${CMAKE_VERSION} VERSION_LESS "3.8")
  message(WARNING "With cmake versions before 3.8, swig-generated files will not always be updated when they should be. Use the clean target and then rebuild to work around this.")
endif()

project(freerdp-java Java CXX)
set(CMAKE_CXX_FLAGS "-std=c++14")

# for development, set default build type to debug
set(CMAKE_BUILD_TYPE "Debug")
  
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

set(FREERDP_SWIG_SOURCES
	src/main/swig/client.i
	src/main/swig/freerdp.i
	)

set(FREERDP_CPP_SOURCES
	src/main/cpp/ClientContext.h
	src/main/cpp/ClientContext.cpp
	src/main/cpp/FreeRDPWrapper.h
	src/main/cpp/FreeRDPWrapper.cpp
	)
  
set_source_files_properties( ${FREERDP_SWIG_SOURCES}
							 PROPERTIES
							 CPLUSPLUS ON
							 )
set(CMAKE_SWIG_FLAGS -package com.nextcentury.savior.freerdp)
include_directories(src/main/cpp)

swig_add_module(
	jfreerdp
	java
	${FREERDP_SWIG_SOURCES}
	${FREERDP_CPP_SOURCES}
	)
swig_link_libraries(jfreerdp freerdp-client2 freerdp2 winpr2)

# configure C++ tests

enable_testing()

include(ExternalProject)
ExternalProject_Add(
  catch
  PREFIX ${CMAKE_BINARY_DIR}/catch
  URL https://github.com/catchorg/Catch2/releases/download/v2.2.2/catch.hpp
  DOWNLOAD_NAME catch.hpp
  DOWNLOAD_NO_EXTRACT TRUE
  DOWNLOAD_NO_PROGRESS TRUE
  DOWNLOAD_DIR catch
  CONFIGURE_COMMAND ""
  BUILD_COMMAND ""
  INSTALL_COMMAND ""
  )

add_library(Catch INTERFACE)
include_directories(build/catch)

# Build a lib w/o any SWIG or Java stuff to test the base C++ code
add_library(jfreerdp-cpp ${FREERDP_CPP_SOURCES})
link_libraries(jfreerdp-cpp freerdp-client2 freerdp2 winpr2)

add_executable(ClientContextTest src/test/cpp/ClientContextTest.cpp)
target_link_libraries(ClientContextTest Catch jfreerdp-cpp)
add_test(NAME ClientContextTest COMMAND ClientContextTest)
