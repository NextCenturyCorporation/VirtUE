/*
 * FreeRDP.h
 *
 *  Created on: Apr 9, 2018
 *      Author: clong
 */

#ifndef SRC_MAIN_CPP_FREERDP_H_
#define SRC_MAIN_CPP_FREERDP_H_

#include <freerdp/freerdp.h>
#include "swighelper.h"

CALLBACK_CLASS_DECL(PostConnectHandler, bool, (freerdp* instance), freerdp*);

/**
 * Proxy for the freerdp struct.
 */
class FreeRDP {
public:
	FreeRDP();
	virtual ~FreeRDP();

	void setPostConnectHandler(PostConnectHandler* handler) {
        PostConnectHandler::setCallback(rdp, handler);
    }
private:
	freerdp* rdp;

    void registerCallbacks() {
        rdp->PostConnect = _postConnect;
    }
    static BOOL _postConnect(freerdp* instance) {
        PostConnectHandler* handler = PostConnectHandler::getCallback(instance);
        if (handler) {
            return handler->apply(instance);
        }
        else {
        	return TRUE;
        }
    }
};


#endif /* SRC_MAIN_CPP_FREERDP_H_ */
