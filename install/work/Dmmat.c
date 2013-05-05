/*
 * Dmmat.c
 *
 * Methods and data for DMMAT boards.
 *
 *  Created on: Aug 3, 2010
 *      Author: dirk
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <time.h>

#include "dscud.h"

#include "Dmmat.h"
#include "log.h"

BYTE result; // returned error code
DSCB dscb; // handle used to refer to the board
DSCCB dsccb; // structure containing board settings
DSCDACS dscdacs; // structure containing DA conversion settings
ERRPARAMS errorParams; // structure for returning error code and error string
char logmsg[1024];

/*
 * Initialize the DMM-AT board. This function passes the various
 * hardware parameters to the driver and resets the hardware.
 */
void dmmatInit(unsigned short address) {
	sprintf(logmsg, ">>> Registering Dmmat board, address=0x%x\n",address);
	mylog(MYLOG_DEBUG, logmsg);

	dsccb.boardtype = DSC_DMMAT;
	dsccb.base_address = address;
	dsccb.int_level = 3;
	if (dscInitBoard(DSC_DMMAT, &dsccb, &dscb) != DE_NONE) {
		dscGetLastError(&errorParams);
		sprintf(logmsg, "dscInitBoard error: %s %s\n", dscGetErrorString(
						errorParams.ErrCode), errorParams.errstring);
		mylog(MYLOG_DEBUG, logmsg);
	}
	sprintf(logmsg,"    Registering ok, dscb=0x%x\n", dscb);
	mylog(MYLOG_DEBUG, logmsg);
	fflush(NULL);

	sprintf(logmsg, "<<< Registered Dmmat board, address=%x, dscb=%x\n", address, dscb);
	mylog(MYLOG_DEBUG, logmsg);
}

/*
 * Reads inputs, as instructed by a request like
 * 	REQ_INP 0x300 D YYN
 * int address: address of boards
 * parms:
 * 	[0] is address, in text - ignored by this routine
 * 	[1]	type D - ignored by this routine
 * 	[2] whether digital in, analog channel 0 and analogy channel 1 need to be read.
 * result: returns string like <code>INP_D 0x300 6 - 240</code><br>
 * so digital 6, analog channel 0 not read, analog channel 1 reads 240.
 */
void dmmatReadInputs(int address, char** parms, char* result) {
	BYTE digi_b;
	char digi[5], ana[5][2];
	int i;

	mylog(MYLOG_DEBUG, ">>> dmmatReadInputs");
	if (parms[2][0] == 'Y') {
		if (dscDIOInputByte(dscb, 0, &digi_b) != DE_NONE) {
			dscGetLastError(&errorParams);
			sprintf(logmsg, "dscDIOInputByte error: %s %s\n",
					dscGetErrorString(errorParams.ErrCode),
					errorParams.errstring);
			mylog(MYLOG_ERROR, logmsg);
		}
		sprintf(logmsg, "    digi in address:0x%x read:%d",address, (int)digi_b);
		mylog(MYLOG_DEBUG, logmsg);
		sprintf(digi,"%d",(int)digi_b);
	} else {
		strcpy(digi,"-");
	}

	for (i=0; i<2; i++) {
		if (parms[2][i+1]=='Y') {
			sprintf(logmsg, "    reading analog input not implemented, return '255' (all off), channel=%d", i);
			mylog(MYLOG_WARN, logmsg);
			strcpy(ana[i],"255");
		} else {
			strcpy(ana[i],"-");
		}
	}

	sprintf(result, "INP_D 0x%x %s %s %s", address, digi, ana[0], ana[1]);
	mylog(MYLOG_DEBUG, "<<< dmmatReadInputs");
}


/*
 * Sets outputs.
 * int address: address of boards
 * parms:
 * 	[0] is address, in text - ignored by this routine
 * 	[1]	type - ignored by this routine
 * 	[2] is digital output value (8 channels), or '-' (don't change)
 * 	[3] is value for analog output channel nr 0, or '-' (don't change)
 * 	[4] is value for analog output channel nr 1, or '-' (don't change)
 * Example message: SET_OUT 0x300 D - 1025 2056
 */
void dmmatSetOutputs(int address, char** parms) {
	int channel, value;

	mylog(MYLOG_DEBUG, ">>> dmmatSetOutputs");

	// Digital output channel
	if (parms[2][0] != '-') {
		sscanf(parms[2], "%d", &value);
		sprintf(logmsg,"    dmmatSetOutputs() digital=%d", value);
		mylog(MYLOG_DEBUG, logmsg);
		if ((result = dscDIOOutputByte(dscb, 0, (BYTE) value)) != DE_NONE) {
			dscGetLastError(&errorParams);
			sprintf( logmsg, "dscDIOOutputByte error: %s %s\n", dscGetErrorString(errorParams.ErrCode), errorParams.errstring );
			mylog(MYLOG_ERROR, logmsg);
		}
	} else {
		sprintf(logmsg,"    dmmatSetOutputs() digital not requested.");
		mylog(MYLOG_DEBUG, logmsg);
	}

	// Analog output channels
	dscdacs.output_codes = (DSCDACODE*) malloc(sizeof(DSCDACODE) * 2);
	for (channel = 0; channel < 2; channel++) {
		if (parms[channel + 3][0] == '-') {
			dscdacs.channel_enable[channel] = FALSE;
		} else {
			dscdacs.channel_enable[channel] = TRUE;
			sscanf(parms[channel + 3], "%d", &value);
			dscdacs.output_codes[channel] = value;
		}
	}

	sprintf(logmsg,"    dmmatSetOutputs() analog [0]=%ld [1]=%ld", (dscdacs.channel_enable[0]?dscdacs.output_codes[0]:-1),(dscdacs.channel_enable[1]?dscdacs.output_codes[1]:-1));
	mylog(MYLOG_DEBUG, logmsg);

	if ((result = dscDAConvertScan(dscb, &dscdacs)) != DE_NONE) {
		dscGetLastError(&errorParams);
		sprintf(logmsg, "dscDAConvertScan error: %s %s\n", dscGetErrorString(
						errorParams.ErrCode), errorParams.errstring);
		mylog(MYLOG_ERROR, logmsg);
		return;
	}

	free(dscdacs.output_codes);
	mylog(MYLOG_DEBUG, "<<< dmmatSetOutputs\n");
}
