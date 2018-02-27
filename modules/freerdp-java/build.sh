#!/bin/bash
#
# Build the project. Check for a sibling build dir.
#
myDir="$(readlink --canonicalize $(dirname $0))"
buildDir="${myDir}-build"

[ -d "${buildDir}" ] && cd "${buildDir}"

exec make "${@}"

 