/*
 * FreeRDP.h
 *
 *  Created on: Apr 9, 2018
 *      Author: clong
 */

#ifndef SRC_MAIN_CPP_FREERDP_H_
#define SRC_MAIN_CPP_FREERDP_H_

#include <freerdp/freerdp.h>
#include <map>
#include <mutex>

#ifdef SWIG
#define CALLBACK_CLASS_SWIGPROPS(CLASS) \
    %ignore CLASS::setCallback;                                         \
    %ignore CLASS::getCallback;                                         \
    %feature("director") CLASS
#else
#define CALLBACK_CLASS_SWIGPROPS(CLASS)
#endif

/**
 * Define a class for managing one type of callback. Not using varargs
 * because swig can't handle it.
 */
#define CALLBACK_CLASS_DECL(CLASS, RETURN, ARGS, KEY)                   \
    CALLBACK_CLASS_SWIGPROPS(CLASS);                                    \
    class CLASS {                                                       \
    public:                                                             \
    virtual RETURN apply ARGS = 0;                                      \
    virtual ~CLASS() {}                                                 \
    static void setCallback(KEY key, CLASS* callback);                  \
    static CLASS* getCallback(KEY key);                                 \
    private:                                                            \
    static std::mutex callbackMapLock;                                  \
    static std::map<const KEY, CLASS* > callbackMap;                    \
    }

#define CALLBACK_CLASS_IMPL(CLASS, RETURN, ARGS, KEY)                   \
    void CLASS::setCallback(KEY key, CLASS* callback) {                 \
        std::lock_guard<std::mutex> lock(callbackMapLock);              \
        callbackMap[key] = callback;                                    \
    }                                                                   \
    CLASS* CLASS::getCallback(KEY key) {                                \
        std::lock_guard<std::mutex> lock(callbackMapLock);              \
        return callbackMap[key];                                        \
    }                                                                   \
    std::mutex CLASS::callbackMapLock;                                  \
    std::map<const KEY, CLASS* > CLASS::callbackMap                     \

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
