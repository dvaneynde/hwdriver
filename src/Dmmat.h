/*
 * Dmmat.h
 *
 * Methods and data for DMMAT boards.
 *
 *  Created on: Aug 3, 2010
 *      Author: dirk
 */

#ifndef DMMAT_H_
#define DMMAT_H_

void dmmatInit(unsigned short address);

/*
 * Sets analog outputs.
 * int address: address of boards
 * parms:
 * 	[0] is address, in text - ignored by this routine
 * 	[1]	type - ignored by this routing
 * 	[2] is digital output value (8 channels), or '-' (don't change)
 * 	[3] is value for analog output channel nr 0, or '-' (don't change)
 * 	[4] is value for analog output channel nr 1, or '-' (don't change)
 * Example message: SET_OUT 0x300 D - 1025 2056
 */
void dmmatSetOutputs(int address, char** parms);

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
void dmmatReadInputs(int address, char** parms, char* result);

#endif /* DMMAT_H_ */
