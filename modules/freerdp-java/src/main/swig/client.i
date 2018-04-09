%module(directors="1") client

%begin %{
    /* prevents conflicting definitions of __int64 between swig and winpr */
#define __INTEL_COMPILER
%}

%{
#include <freerdp/client.h>
#include "ClientContext.h"
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

%import "freerdp/api.h"
%include "freerdp/client.h"
%include "freerdp/settings.h"
%feature("director") ClientEntryPoints;
%include "ClientContext.h"

 /*
  * steps for using the client API
  * 1. initialize an instance of RDP_CLIENT_ENTRY_POINTS (a struct of callbacks)
  * 2. make a new rdpContext with freerdp_client_context_new
  * 3. set stuff in context->settings
  * 4. call freerdp_client_start
  * 5. 
  *
  */
