/*
 * log.c
 *
 *  Created on: Sep 1, 2010
 *      Author: dirk
 */
#include <stdio.h>
#include "log.h"

int highestlevel = MYLOG_INFO;

void mylogSetHighestLevel(short lvl) {
	highestlevel = lvl;
}

void mylog(short lvl, char* msg) {
	if (lvl >= highestlevel) {
		printf("%d - %s\n", lvl, msg);
		fflush(NULL);
	}
}

/*
 * For Tests
 mylog(MYLOG_TRACE,"Trace");
 mylog(MYLOG_DEBUG,"Debug");
 mylog(MYLOG_INFO,"Info");
 mylog(MYLOG_WARN,"Warn");
 mylog(MYLOG_ERROR,"Error");
 mylog(MYLOG_FATAL,"Fatal");
 *
 */
