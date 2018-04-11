// Intended to be included by freerdp.i

%{
#include <freerdp/client.h>
#include "ClientContext.h"
    %}

%include "freerdp/client.h"
%feature("director") ClientEntryPoints;
%import "swighelper.h"
%include "ClientContext.h"

 /*
  * steps for using the client API
  * 1. initialize an instance of RDP_CLIENT_ENTRY_POINTS (a struct of callbacks)
  * 2. make a new rdpContext with freerdp_client_context_new
  * 3. set stuff in context->settings
  * 4. set client start callback. it should create a thread and set the thread field in the context, for example with CreateThread in winpr/thread.h
  * 5. call freerdp_client_start
  *
  */
