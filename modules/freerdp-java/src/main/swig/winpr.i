%module(directors="1") winpr

%begin %{
    /* prevents conflicting definitions of __int64 between swig and winpr */
#define __INTEL_COMPILER
%}

%{
#include <winpr/collections.h>
    %}

%include "stdint.i"
%apply uint32_t { UINT, UINT32, ULONG, DWORD, DWORD32 };
%apply uint64_t { ULONGLONG, UINT64, DWORD64, DWORDLONG, QWORD };
%rename("isSynchronized") "synchronized";
%include "winpr/winpr.h"
%include "winpr/collections.h"
