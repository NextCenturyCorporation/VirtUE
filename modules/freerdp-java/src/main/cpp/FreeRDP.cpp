/*
 * FreeRDP.cpp
 *
 *  Created on: Feb 22, 2018
 *      Author: clong
 */


#include "FreeRDP.h"

FreeRDP& FreeRDP::getInstance(const freerdp* rdp) {
	std::lock_guard<std::mutex> lock(instanceMapLock);
	return *instanceMap[rdp];
}

FreeRDP::FreeRDP() {
	instance = freerdp_new();
	std::lock_guard<std::mutex> lock(instanceMapLock);
	instanceMap[instance] = this;
	registerCallbacks();
}

FreeRDP::~FreeRDP() {
	if (instance) {
		std::lock_guard<std::mutex> lock(instanceMapLock);
		instanceMap.erase(instance);
		freerdp_free(instance);
		instance = 0;
	}
}

bool FreeRDP::contextNew(rdpContext* context) {
	return freerdp_context_new(instance);
}

void FreeRDP::contextFree(rdpContext* context) {
	freerdp_context_free(instance);
}

void FreeRDP::registerCallbacks() {
	instance->PreConnect = FreeRDP::_preConnect;
	instance->PostConnect = FreeRDP::_postConnect;
}

int FreeRDP::_preConnect(freerdp* instance) {
	return FreeRDP::getInstance(instance).preConnect();
}

int FreeRDP::_postConnect(freerdp* instance) {
	return FreeRDP::getInstance(instance).postConnect();
}
