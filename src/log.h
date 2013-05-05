/*
 * log.h
 *
 *  Created on: Sep 1, 2010
 *      Author: dirk
 */

#ifndef LOG_H_
#define LOG_H_

#define MYLOG_TRACE	0
#define MYLOG_DEBUG	1
#define MYLOG_INFO	5
#define MYLOG_WARN	6
#define MYLOG_ERROR	7
#define MYLOG_FATAL	8

void mylogSetHighestLevel(short lvl);
void mylog(short lvl, char* msg);

#endif /* LOG_H_ */
