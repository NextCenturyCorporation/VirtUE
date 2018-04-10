%module(directors="1") freerdpmodule

%begin %{
    /* prevents conflicting definitions of __int64 between swig and winpr */
#define __INTEL_COMPILER
%}

%{
#include <freerdp/freerdp.h>
#include "FreeRDPWrapper.h"
#include "FreeRDP.h"
    %}

%apply bool { BOOL };

%import "freerdp/api.h"
%include "freerdp/settings.h"
%include "freerdp/freerdp.h"

%feature("director") BoolCallback;
%include "FreeRDPWrapper.h"
%import "swighelper.h"
%include "FreeRDP.h"
