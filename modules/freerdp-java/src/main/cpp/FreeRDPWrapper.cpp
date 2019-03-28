/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
