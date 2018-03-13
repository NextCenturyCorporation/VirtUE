/*
 * FreeRDPWrapper.cpp
 *
 *  Created on: Feb 22, 2018
 *      Author: clong
 */


#include "FreeRDPWrapper.h"

std::mutex FreeRDPWrapper::instanceMapLock;
std::map<const freerdp*, FreeRDPWrapper*> FreeRDPWrapper::instanceMap;

FreeRDPWrapper& FreeRDPWrapper::getInstance(const freerdp* rdp) {
	std::lock_guard<std::mutex> lock(instanceMapLock);
	return *instanceMap[rdp];
}

FreeRDPWrapper::FreeRDPWrapper() {
	instance = freerdp_new();
	std::lock_guard<std::mutex> lock(instanceMapLock);
	instanceMap[instance] = this;
	registerCallbacks();
}

FreeRDPWrapper::~FreeRDPWrapper() {
	if (instance) {
		std::lock_guard<std::mutex> lock(instanceMapLock);
		instanceMap.erase(instance);
		freerdp_free(instance);
		instance = 0;
	}
}

freerdp* FreeRDPWrapper::getInstance() {
	return instance;
}

bool FreeRDPWrapper::contextNew(rdpContext* context) {
	return freerdp_context_new(instance);
}

void FreeRDPWrapper::contextFree(rdpContext* context) {
	freerdp_context_free(instance);
}

void FreeRDPWrapper::registerCallbacks() {
	instance->PreConnect = FreeRDPWrapper::_preConnect;
	instance->PostConnect = FreeRDPWrapper::_postConnect;
}

int FreeRDPWrapper::_preConnect(freerdp* instance) {
	return FreeRDPWrapper::getInstance(instance).preConnect();
}

int FreeRDPWrapper::_postConnect(freerdp* instance) {
	return FreeRDPWrapper::getInstance(instance).postConnect();
}
