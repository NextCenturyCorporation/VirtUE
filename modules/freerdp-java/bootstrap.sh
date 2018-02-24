#!/bin/bash
#
cmake \
	  -G 'Eclipse CDT4 - Unix Makefiles' \
	  -DCMAKE_CXX_FLAGS='-std=c++14' \
	  $(dirname "$0")
