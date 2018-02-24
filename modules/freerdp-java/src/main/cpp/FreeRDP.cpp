/*
 * FreeRDP.cpp
 *
 *  Created on: Feb 22, 2018
 *      Author: clong
 */


#include "FreeRDP.h"

FreeRDP& FreeRDP::getInstance(const freerdp& rdp) const {
	std::lock_guard<std::mutex> lock(instanceMapLock);
	return instanceMap[rdp];
}

FreeRDP::FreeRDP() {
	instance = freerdp_new();
	std::lock_guard<std::mutex> lock(instanceMapLock);
	instanceMap[*instance] = *this;
	registerCallbacks();
}

FreeRDP::~FreeRDP() {
	std::lock_guard<std::mutex> lock(instanceMapLock);
	instanceMap.erase(*instance);
	freerdp_free(instance);
}

bool FreeRDP::contextNew(rdpContext* context) {
	return freerdp_context_new(instance);
}

bool FreeRDP::contextFree(rdpContext* context) {
	return freerdp_context_free(instance);
}

void FreeRDP::registerCallbacks() {
	instance->PreConnect = FreeRDP::_preConnect;
	instance->PostConnect = FreeRDP::_postConnect;
}

bool FreeRDP::_preConnect(freerdp* instance) {
	return FreeRDP::getInstance(*instance).preConnect();
}

bool FreeRDP::_postConnect(freerdp* instance) {
	return FreeRDP::getInstance(*instance).postConnect();
}

bogus;
