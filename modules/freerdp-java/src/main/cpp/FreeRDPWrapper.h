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
