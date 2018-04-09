/*
 * ClientContextTest.cpp
 *
 *  Created on: Apr 6, 2018
 *      Author: clong
 */

#define CATCH_CONFIG_MAIN
#include "catch.hpp"
#include "ClientContext.h"

TEST_CASE( "ClientContext->start()", "[ClientContext]") {
	ClientContext* cc = new ClientContext(0);
	REQUIRE ( cc != 0  );
	REQUIRE ( cc->start() == 0 );
}
