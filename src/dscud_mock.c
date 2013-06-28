/*
 * dscud_mock.c
 *
 *  Created on: Aug 7, 2010
 *      Author: dirk
 */
#include <stdio.h>
#include "target.h"

#ifdef OSX

#include "dscud.h"

BYTE DSCUDAPICALL dscInitBoard(BYTE boardtype, DSCCB* dsccb, DSCB* board) {
	printf("DIAMOND dscInitBoard called.\n");
	*board = 0x123;
	return DE_NONE;
}

BYTE DSCUDAPICALL dscGetLastError(ERRPARAMS* errparams) {
	printf("DIAMOND dscGetLastError called, DE_NONE returned.\n");
	return DE_NONE;
}

BYTE DSCUDAPICALL dscInit(WORD version) {
	printf("DIAMOND dscInit called.\n");
	return DE_NONE;
}

DSCUDAPICALL char* dscGetErrorString(BYTE error_code) {
	printf("DIAMOND dscGetErrorString called.\n");
	return "DUMMY DLVM";
}

BYTE DSCUDAPICALL dscFree(void) {
	printf("DIAMOND dscFree called.\n");
	return DE_NONE;
}

BYTE DSCUDAPICALL dscDAConvertScan(DSCB board, DSCDACS *dscdacs) {
	printf("DIAMOND dscDAConvertScan called.\n");
	return DE_NONE;
}

BYTE DSCUDAPICALL dscDIOOutputByte(DSCB board, BYTE port, BYTE digital_value) {
	printf("DIAMOND dscDIOOutputByte called.\n");
	return DE_NONE;
}
BYTE DSCUDAPICALL dscDIOInputByte(DSCB board, BYTE port, BYTE* digital_value) {
	printf("DIAMOND dscDIOInputByte called, return 6.\n");
	(*digital_value) = 6;
	return DE_NONE;
}

BYTE DSCUDAPICALL dscADCodeToVoltage(DSCB board, DSCADSETTINGS adsettings,
		DSCSAMPLE adcode, DFLOAT *voltage) {
	printf("DIAMOND dscADCodeToVoltage called, return 5.\n");
	(*voltage) = 5;
	return DE_NONE;
}

BYTE DSCUDAPICALL dscADSample(DSCB board, DSCSAMPLE* sample) {
	printf("DIAMOND dscADSample called, return 2048.\n");
	(*sample) = 2048;
	return DE_NONE;
}

BYTE DSCUDAPICALL dscADSetSettings(DSCB board, DSCADSETTINGS* settings) {
	printf("DIAMOND dscADSetSettings called, NOP.\n");
	return DE_NONE;
}

#endif
