/*
 * opalmm.h
 *
 *  Created on: Feb 21, 2010
 *      Author: dirk
 */

#ifndef OPALMM_H_
#define OPALMM_H_

#include "target.h"

void opalmmInit(unsigned short address);
void opalmmClose(unsigned short address);

void opalmmSetDigiOut(int port, int value);
int opalmmReadDigIn(int port);

#endif /* OPALMM_H_ */
