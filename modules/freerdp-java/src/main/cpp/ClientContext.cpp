#include "ClientContext.h"
#include "ConcurrentMap.h"
#include <winpr/thread.h>

CALLBACK_CLASS_IMPL(ClientThreadRunner, bool, (freerdp* instance), rdpContext*);

// callback lookups
static ConcurrentMap< rdpContext*, ClientContext* > contextMap;
/**
 * Entry points for use during our call to freerdp_client_context_new,
 * for access by our globalInit callback.
 */
static thread_local ClientEntryPoints* contextNewEntryPoints = 0;
/**
 * Entry points for use during our call to freerdp_client_context_free,
 * for access by our globalInit callback.
 */
static thread_local ClientEntryPoints* contextFreeEntryPoints = 0;

ClientContext::ClientContext(ClientEntryPoints* ep)
    : entryPoints(ep) {
    RDP_CLIENT_ENTRY_POINTS cEntryPoints;
    setEntryPoints(cEntryPoints);
    cEntryPoints.ContextSize = sizeof(rdp_client_context);

    contextNewEntryPoints = ep;
    context = freerdp_client_context_new(&cEntryPoints);
    contextNewEntryPoints = 0;
    contextMap[context] = this;
}

ClientContext::~ClientContext() {
    contextMap.erase(context);
    contextFreeEntryPoints = entryPoints;
    freerdp_client_context_free(context);
    contextFreeEntryPoints = 0;
}

int ClientContext::start() {
    return freerdp_client_start(context);
}

int ClientContext::stop() {
    return freerdp_client_stop(context);
}

void ClientContext::setEntryPoints(RDP_CLIENT_ENTRY_POINTS& clientEntryPoints) const {
	ZeroMemory(&clientEntryPoints, sizeof(RDP_CLIENT_ENTRY_POINTS));
	clientEntryPoints.Size = sizeof(RDP_CLIENT_ENTRY_POINTS);
	clientEntryPoints.Version = RDP_CLIENT_INTERFACE_VERSION;
    if (entryPoints) {
        clientEntryPoints.GlobalInit = globalInit;
        clientEntryPoints.GlobalUninit = globalUninit;
        clientEntryPoints.ClientNew = clientNew;
        clientEntryPoints.ClientFree = clientFree;
        clientEntryPoints.ClientStart = clientStart;
        clientEntryPoints.ClientStop = clientStop;
    }
}

BOOL ClientContext::globalInit() {
    if (contextNewEntryPoints) {
    	return contextNewEntryPoints->globalInit();
    }
    else {
    	return TRUE;
    }
}

void ClientContext::globalUninit() {
}

BOOL ClientContext::clientNew(freerdp* instance, rdpContext* context) {
    ClientContext* cc = contextMap[context];
    ClientEntryPoints* entryPoints = cc == 0 ? 0 : cc->entryPoints;
    if (entryPoints) {
        return entryPoints->clientNew(instance, cc);
    }
    else {
        return TRUE;
    }
}

void ClientContext::clientFree(freerdp* instance, rdpContext* context) {
    ClientContext* cc = contextMap[context];
    ClientEntryPoints* entryPoints = cc == 0 ? 0 : cc->entryPoints;
    if (entryPoints) {
        entryPoints->clientFree(instance, cc);
    }
}

int ClientContext::clientStart(rdpContext* context) {
    ClientContext* cc = contextMap[context];
    ClientEntryPoints* entryPoints = cc == 0 ? 0 : cc->entryPoints;
    if (entryPoints) {
        return entryPoints->clientStart(cc);
    }
    else {
        return 0;
    }
}

int ClientContext::clientStop(rdpContext* context) {
    ClientContext* cc = contextMap[context];
    ClientEntryPoints* entryPoints = cc == 0 ? 0 : cc->entryPoints;
    if (entryPoints) {
        return entryPoints->clientStop(cc);
    }
    else {
        return 0;
    }
}

bool ClientContext::createThread(ClientThreadRunner* runner) {
	ClientThreadRunner::setCallback(context, runner);
	HANDLE thread = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)
			&createThreadCallback, context->instance, 0, NULL);
}

void* ClientContext::createThreadCallback(void* arg) {
	rdpContext* context = static_cast<rdpContext*>(arg);
	ClientThreadRunner* runner = ClientThreadRunner::getCallback(context);
	runner->apply(context->instance);
	ClientThreadRunner::removeCallback(context);
}

int ClientContext::waitForThread() {
	HANDLE thread = freerdp_client_get_thread(context);
	WaitForSingleObject(thread, INFINITE);
	DWORD dwExitCode;
	GetExitCodeThread(thread, &dwExitCode);

	return dwExitCode;
}
