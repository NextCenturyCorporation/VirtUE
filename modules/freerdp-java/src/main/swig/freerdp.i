%module(directors="1") freerdp

%begin %{
    /* prevents conflicting definitions of __int64 between swig and winpr */
#define __INTEL_COMPILER
%}

%{
#include <freerdp/freerdp.h>
#include "FreeRDP.h"
    %}

%feature("director") FreeRDP;

%import "freerdp/api.h"
%include "freerdp/settings.h"
%include "freerdp/freerdp.h"
%include "FreeRDP.h"
