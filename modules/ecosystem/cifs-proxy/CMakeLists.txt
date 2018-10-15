cmake_minimum_required(VERSION 3.10)
project(cifs-proxy)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake")
  
find_package(Krb5 REQUIRED gssapi)

add_executable(getserviceticket
  src/cpp/getserviceticket.cpp
  )
target_include_directories(getserviceticket INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(getserviceticket ${KRB5_LIBRARIES})

add_executable(importcreds
  src/cpp/importcreds.cpp
  )
target_include_directories(importcreds INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(importcreds ${KRB5_LIBRARIES})

add_executable(switchprincipal
  src/cpp/switchprincipal.cpp
  )
target_include_directories(switchprincipal INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(switchprincipal ${KRB5_LIBRARIES})
