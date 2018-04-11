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

/* When using directors, multiple Java objects can refer to the same C++ object. */

%typemap(javacode) SWIGTYPE %{
  public boolean equals(Object obj) {
    boolean equal = false;
    if (obj instanceof $javaclassname)
      equal = ((($javaclassname)obj).swigCPtr == this.swigCPtr);
    return equal;
  }
  public int hashCode() {
    return (int)swigCPtr;
  }
%}

%import "winpr.i"

// strip "freerdp_" from the start of identifiers
%rename("%(regex:/^freerdp_(?!new)(.*)/\\1/)s", regextarget=1, fullname=1) "^freerdp_";

%import "freerdp/api.h"
%include "freerdp/settings.h"
%include "freerdp/freerdp.h"

%feature("director") BoolCallback;
%include "FreeRDPWrapper.h"
%import "swighelper.h"
%include "FreeRDP.h"

%include "client.i"
