This module uses a combined gradle + cmake build.

CMake runs swig to generate Java and native code and compiles the
native code into a shared library (.e.g., .so, .DLL).

Gradle then builds the generated Java code into a jar.
