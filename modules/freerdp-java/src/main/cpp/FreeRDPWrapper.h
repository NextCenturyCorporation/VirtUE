/*
 * FreeRDPWrapper.h
 *
 *  Created on: Feb 22, 2018
 *      Author: clong
 */

#ifndef SRC_MAIN_CPP_FREERDPWRAPPER_H_
#define SRC_MAIN_CPP_FREERDPWRAPPER_H_

#include <freerdp/freerdp.h>
#include <map>
#include <mutex>

class FreeRDPWrapper {
public:
	FreeRDPWrapper();
	virtual ~FreeRDPWrapper();

	virtual bool preConnect() = 0;
	virtual bool postConnect() = 0;

	virtual bool contextNew(rdpContext* context);
	virtual void contextFree(rdpContext* context);

	/** to call functions that haven't been exported yet */
	virtual freerdp* getInstance();
protected:
	freerdp* instance;
	// If we ever need high performance, we could use shared_mutex from boost
	static std::mutex instanceMapLock;
	static std::map<const freerdp*, FreeRDPWrapper*> instanceMap;
	static FreeRDPWrapper& getInstance(const freerdp* rdp);

	virtual void registerCallbacks();
	static int _preConnect(freerdp* instance);
	static int _postConnect(freerdp* instance);
};

#endif /* SRC_MAIN_CPP_FREERDPWRAPPER_H_ */
