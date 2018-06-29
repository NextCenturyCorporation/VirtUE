cmake_minimum_required(VERSION 3.10)
project(cifs-proxy)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake")
  
find_package(Krb5 REQUIRED gssapi)

add_executable(makeccache
  src/cpp/makeccache.cpp
  )
target_include_directories(makeccache INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(makeccache ${KRB5_LIBRARIES})
