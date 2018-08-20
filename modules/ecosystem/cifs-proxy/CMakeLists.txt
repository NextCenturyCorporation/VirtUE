cmake_minimum_required(VERSION 3.10)
project(cifs-proxy)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake")
  
find_package(Krb5 REQUIRED gssapi)

add_executable(makeccache
  src/cpp/makeccache.cpp
  )
target_include_directories(makeccache INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(makeccache ${KRB5_LIBRARIES})

add_executable(getserviceticket
  src/cpp/getserviceticket.cpp
  )
target_include_directories(getserviceticket INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(getserviceticket ${KRB5_LIBRARIES})

add_executable(gsscache
  src/cpp/gsscache.cpp
  )
target_include_directories(gsscache INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(gsscache ${KRB5_LIBRARIES})

add_executable(both
  src/cpp/both.cpp
  )
target_include_directories(both INTERFACE ${KRB5_INCLUDE_DIRS})
target_link_libraries(both ${KRB5_LIBRARIES})
