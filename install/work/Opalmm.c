/*
 *  opalmm.c
 *  
 *  Created by Dirk Vaneynde on 27/12/09.
 *  Copyright 2009 dlv mechanografie bvba. All rights reserved.
 *
 * example.c: very simple example of port I/O
 *
 * This code does nothing useful, just a port write, a pause,
 * and a port read. Compile with `gcc -O2 -o example example.c',
 * and run as root with `./example'. 
 */

#include <stdio.h>

#include <stdlib.h>
#include <math.h>
#include <time.h>

#include "Opalmm.h"
#include "log.h"

char logmsg[1024];

#ifdef OSX

void opalmmInit(unsigned short address) {
	sprintf(logmsg, "Initializing Opalmm board, address=0x%x\n",address);
	mylog(MYLOG_INFO, logmsg);
	printf("Initializing Opalmm board, address=0x%x\n",address);
}

void opalmmClose(unsigned short address) {
	printf("Close Opalmm board, address=0x%x\n",address);
}

void opalmmSetDigiOut(int port, int value) {
	//printf("opalmm.c/OSX setDigOut(), port=0x%x value=%d\n",port,value);
}
int opalmmReadDigIn(int port) {
	static int r = 1;
	//printf("opalmm.c/OSX readDigIn, port=0x%x, returning %d\n",port,r);
	return r++;
}

#else /* hierna niet OSX */

#include <sys/io.h>
#include <errno.h>

/*
 * Initialize the DMM-AT board. This function passes the various
 * hardware parameters to th
 * e driver and resets the hardware.
 */
void opalmmInit(unsigned short address) {
	sprintf(logmsg, "Initializing Opalmm board, address=0x%x\n",address);
	mylog(MYLOG_INFO, logmsg);
	if (ioperm(address, 2, 1)) {
		sprintf(logmsg, "Opening Opalmm port via ioperm: %s", strerror(errno));
		mylog(MYLOG_FATAL,logmsg);
		return;
	}
}

void opalmmClose(unsigned short address) {
	if (ioperm(address, 2, 0)) {
		sprintf(logmsg, "Closing Opalmm port via ioperm: %s", strerror(errno));
		mylog(MYLOG_WARN,logmsg);
	}
}

void opalmmSetDigiOut(int port, int value) {
	//printf("opalmm.c/REAL setDigOut(), port=0x%x value=%d\n",port,value);
	outb(value, port);
}

int opalmmReadDigIn(int port) {
	int value = inb(port);
	//printf("opalmm.c/REAL readDigIn, port=0x%x, returning %d\n",port,value);
	return value;
}


#endif
