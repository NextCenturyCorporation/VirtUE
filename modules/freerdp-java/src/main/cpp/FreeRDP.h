/*
 * FreeRDP.h
 *
 *  Created on: Feb 22, 2018
 *      Author: clong
 */

#ifndef SRC_MAIN_CPP_FREERDP_H_
#define SRC_MAIN_CPP_FREERDP_H_

#include <freerdp/freerdp.h>
#include <map>
#include <mutex>

class FreeRDP {
public:
	FreeRDP();
	virtual ~FreeRDP();

	virtual bool preConnect() = 0;
	virtual bool postConnect() = 0;

	virtual bool contextNew(rdpContext* context);
	virtual void contextFree(rdpContext* context);

protected:
	freerdp* instance;
	// If we ever need high performance, we could use shared_mutex from boost
	static std::mutex instanceMapLock;
	static std::map<const freerdp*, FreeRDP*> instanceMap;
	static FreeRDP& getInstance(const freerdp* rdp);

	virtual void registerCallbacks();
	static int _preConnect(freerdp* instance);
	static int _postConnect(freerdp* instance);
};


#endif /* SRC_MAIN_CPP_FREERDP_H_ */
