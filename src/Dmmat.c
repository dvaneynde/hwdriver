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

ERRPARAMS errorParams; // structure for returning error code and error string
char logmsg[1024];

struct address2dscbStruct {
	unsigned short address;
	DSCB dscb;
};
struct address2dscbStruct address2dscbs[10];
int nrBoards = 0;

DSCB findDSCBbyAddress(unsigned short address) {
	int i;
	for (i = 0; i < nrBoards; i++)
		if (address2dscbs[i].address == address)
			return address2dscbs[i].dscb;
	return -1;
}

/*
 * Initialize the DMM-AT board. This function passes the various
 * hardware parameters to the driver and resets the hardware.
 */
void dmmatInit(unsigned short address) {
	DSCCB dsccb; // structure containing board settings
	DSCB dscb; // handle used to refer to the boards
	sprintf(logmsg, ">>> Registering Dmmat board, address=0x%x", address);
	mylog(MYLOG_DEBUG, logmsg);

	// TODO als address2dscbs al een entry heeft voor dit adres, herbruiken; maar ik ga ervan uit dat we voorlopig maar 1 keer initialiseren.
	//dsccb.boardtype = DSC_DMMAT;
	dsccb.base_address = address;
	dsccb.int_level = 7;// TODO was 3, maar veranderd naar echte interrupt op kaart; moet configureerbaar !
	if (dscInitBoard(DSC_DMMAT, &dsccb, &dscb) != DE_NONE) {
		dscGetLastError(&errorParams);
		sprintf(logmsg, "dscInitBoard error: %s %s",
				dscGetErrorString(errorParams.ErrCode), errorParams.errstring);
		mylog(MYLOG_ERROR, logmsg);
	} else {
		address2dscbs[nrBoards].address = address;
		address2dscbs[nrBoards].dscb = dscb;
		nrBoards++;
		sprintf(logmsg, "    Registering ok, dscb=0x%x (#Boards=%d)", dscb,
				nrBoards);
		mylog(MYLOG_INFO, logmsg);
	}
	fflush(NULL );

	sprintf(logmsg, "<<< Registered Dmmat board, address=%x", address);
	mylog(MYLOG_DEBUG, logmsg);
}

/*
 * Reads inputs, as instructed by a request like
 * 	REQ_INP 0x300 D YYN
 * int address: address of boards
 * parms:
 * 	[0] is address, needed to find DSCB
 * 	[1]	type D - ignored by this routine
 * 	[2] whether digital in, analog channel 0 and analogy channel 1 need to be read.
 * result: returns string like <code>INP_D 0x300 6 - 240</code><br>
 * so digital 6, analog channel 0 not read, analog channel 1 reads 240.
 */
void dmmatReadInputs(unsigned short address, char** parms, char* result) {
	DSCB dscb;
	BYTE digi_b;
	char digi[5], ana[2][5];
	int i;
	DSCADSETTINGS dscadsettings; // structure containing A/D conversion settings
	DSCSAMPLE sample;  // sample reading
	DFLOAT voltage;

	mylog(MYLOG_DEBUG, ">>> dmmatReadInputs");
	dscb = findDSCBbyAddress(address);
	if (dscb < 0) {
		sprintf(logmsg,
				"Board for address 0x%x not initialized? Command ignored.",
				address);
		mylog(MYLOG_WARN, logmsg);
		strcpy(result, "");
		return;
	}
	sprintf(logmsg, "    found dscb=0x%x", dscb);
	mylog(MYLOG_DEBUG, logmsg);

	if (parms[2][0] == 'Y') {
		if (dscDIOInputByte(dscb, 0, &digi_b) != DE_NONE) {
			dscGetLastError(&errorParams);
			sprintf(logmsg, "dscDIOInputByte error: %s %s",
					dscGetErrorString(errorParams.ErrCode),
					errorParams.errstring);
			mylog(MYLOG_ERROR, logmsg);
			strcpy(digi, "-"); // TODO 'E' zetten voor error? Geen probleem voor java code?
		} else {
			sprintf(logmsg, "    digi in address:0x%x read:%d", address,
					(int )digi_b);
			mylog(MYLOG_DEBUG, logmsg);
			sprintf(digi, "%d", (int )digi_b);
		}
	} else {
		strcpy(digi, "-");
	}

	for (i = 0; i < 2; i++) {
		if (parms[2][i + 1] == 'Y') {
			memset(&dscadsettings, 0, sizeof(DSCADSETTINGS));
			dscadsettings.range = (BYTE) 0;
			dscadsettings.polarity = (BYTE) 1;
			dscadsettings.gain = (BYTE) 0;
			dscadsettings.load_cal = (BYTE) 0;
			dscadsettings.current_channel = (BYTE ) i;
			if (dscADSetSettings( dscb, &dscadsettings ) != DE_NONE )
			{
				dscGetLastError(&errorParams);
				sprintf(logmsg, "    Error setting AD settings, channel=%d: %s %s",
						i, dscGetErrorString(errorParams.ErrCode),
						errorParams.errstring);
				mylog(MYLOG_WARN, logmsg);
				strcpy(ana[i], "-");
				continue;
			}
			if (dscADSample(dscb, &sample) != DE_NONE) {
				dscGetLastError(&errorParams);
				sprintf(logmsg, "    Error reading sample, channel=%d: %s %s",
						i, dscGetErrorString(errorParams.ErrCode),
						errorParams.errstring);
				mylog(MYLOG_WARN, logmsg);
				strcpy(ana[i], "-");
				continue;
			}

			if (dscADCodeToVoltage(dscb, dscadsettings, sample,
					&voltage) != DE_NONE) {
				dscGetLastError(&errorParams);
				sprintf(logmsg,
						"    Error converting to voltage, channel=%d: %s %s", i,
						dscGetErrorString(errorParams.ErrCode),
						errorParams.errstring);
				mylog(MYLOG_WARN, logmsg);
				strcpy(ana[i], "-");
				continue;
			}

			sprintf(logmsg,
					"    ana in address:0x%x channel=%d read:%d voltage=%5.3lfV",
					address, i, sample, voltage);
			mylog(MYLOG_DEBUG, logmsg);
			sprintf(ana[i], "%d", sample);
		} else {
			strcpy(ana[i], "-");
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
	DSCB dscb;
	int channel, value;
	DSCDACS dscdacs; // structure containing DA conversion settings
	BYTE result; // returned error code

	sprintf(logmsg, ">>> dmmatSetOutputs, address=0x%x", address);
	mylog(MYLOG_DEBUG, logmsg);
	dscb = findDSCBbyAddress(address);
	if (dscb < 0) {
		sprintf(logmsg,
				"Board for address 0x%x not initialized? Command ignored.",
				address);
		mylog(MYLOG_WARN, logmsg);
		return;
	}
	sprintf(logmsg, "    found dscb=0x%x", dscb);
	mylog(MYLOG_DEBUG, logmsg);

	// Digital output channel
	if (parms[2][0] != '-') {
		sscanf(parms[2], "%d", &value);
		sprintf(logmsg, "    dmmatSetOutputs() digital=%d", value);
		mylog(MYLOG_DEBUG, logmsg);
		if ((result = dscDIOOutputByte(dscb, 0, (BYTE) value)) != DE_NONE) {
			dscGetLastError(&errorParams);
			sprintf(logmsg, "dscDIOOutputByte error: %s %s",
					dscGetErrorString(errorParams.ErrCode),
					errorParams.errstring);
			mylog(MYLOG_ERROR, logmsg);
		}
	} else {
		sprintf(logmsg, "    dmmatSetOutputs() digital not requested.");
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
			sprintf(logmsg, "    analog out %d enabled, value=%d", channel,
					value);
			mylog(MYLOG_DEBUG, logmsg);
			dscdacs.output_codes[channel] = (DSCDACODE) value;
		}
	}

	if ((result = dscDAConvertScan(dscb, &dscdacs)) != DE_NONE) {
		dscGetLastError(&errorParams);
		sprintf(logmsg, "dscDAConvertScan error: %s %s",
				dscGetErrorString(errorParams.ErrCode), errorParams.errstring);
		mylog(MYLOG_ERROR, logmsg);
	}

	free(dscdacs.output_codes);
	mylog(MYLOG_DEBUG, "<<< dmmatSetOutputs");
}
