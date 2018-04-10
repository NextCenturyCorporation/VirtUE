#ifndef _CLIENT_CONTEXT_H_
#define _CLIENT_CONTEXT_H_

#include <freerdp/client.h>
#include "swighelper.h"

class ClientContext;

/**
 * Callbacks used by ClientContext.
 */
class ClientEntryPoints {
public:
    virtual bool globalInit() = 0;
    virtual void globalUninit() = 0;
    virtual bool clientNew(freerdp* instance, ClientContext* context) = 0;
    virtual void clientFree(freerdp* instance, ClientContext* context) = 0;
    virtual int clientStart(ClientContext* context) = 0;
    virtual int clientStop(ClientContext* context) = 0;
    virtual ~ClientEntryPoints() {}
};

CALLBACK_CLASS_DECL(ClientThreadRunner, bool, (freerdp* instance), rdpContext*);

/**
 * A C++ wrapper around the high-level freerdp client API.
 */
class ClientContext {
public:
    ClientContext(ClientEntryPoints* entryPoints);
    ~ClientContext();

    rdpSettings* getSettings() { return context->settings; }
    freerdp* getInstance() { return context->instance; }
    int start();
    int stop();
    bool createThread(ClientThreadRunner* runner);
    /**
     * Wait for the instance thread to finish.
     *
     * @return its exit code
     */
    int waitForThread();
private:
    ClientEntryPoints* const entryPoints;
    rdpContext* context;

    void setEntryPoints(RDP_CLIENT_ENTRY_POINTS& cEntryPoints) const;
    static void* createThreadCallback(void* arg);

    // callbacks for ClientEntryPoints:
    static BOOL globalInit();
    static void globalUninit();
    static BOOL clientNew(freerdp* instance, rdpContext* context);
    static void clientFree(freerdp* instance, rdpContext* context);
    static int clientStart(rdpContext* context);
    static int clientStop(rdpContext* context);
};

#endif
