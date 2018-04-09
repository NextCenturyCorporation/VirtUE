/*
 * FreeRDPWrapper.cpp
 *
 *  Created on: Feb 22, 2018
 *      Author: clong
 */


#include "FreeRDPWrapper.h"
#include <iostream>

static std::mutex preConnectCallbacksLock;
static std::map<const freerdp*, BoolCallback* > preConnectCallbacks;

FreeRDPWrapper::FreeRDPWrapper()
    : instance(freerdp_new()) {
	registerCallbacks();
}

FreeRDPWrapper::~FreeRDPWrapper() {
	std::lock_guard<std::mutex> lock(preConnectCallbacksLock);
	preConnectCallbacks.erase(instance);
	freerdp_free(instance);
}

freerdp* FreeRDPWrapper::getInstance() const {
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

void FreeRDPWrapper::registerPreConnect(BoolCallback* cb) {
	std::lock_guard<std::mutex> lock(preConnectCallbacksLock);
	preConnectCallbacks[instance] = cb;
}
int FreeRDPWrapper::_preConnect(freerdp* instance) {
	std::lock_guard<std::mutex> lock(preConnectCallbacksLock);
	BoolCallback* cb = preConnectCallbacks[instance];
	if (cb) {
		return cb->apply(instance);
	}
	else {
		return 0;
	}
}

int FreeRDPWrapper::_postConnect(freerdp* instance) {
	std::cerr << "postConnect not yet implemented" << std::endl;
	return 0;
}
