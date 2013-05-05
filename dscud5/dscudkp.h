/*
 * This code is distributed under an open source license which is commonly
 * called a "BSD" style license.  This license applies only to souce files
 * "dscudkp.h" and "dscudkp.c" in this directory.  The rest of the Diamond
 * Systems Universal Driver ("libdscud5.a") remains proprietary software.
 */

/*
 * Copyright (c) 2003, Diamond Systems Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Diamond Systems Corporation nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
*/
#ifndef DSCUDKP_H
#define DSCUDKP_H

#include "dscud.h"

/*
 * This struct is used to pass board configuration information between the
 * dscud library and the dscud kernel plugin.  Do not change this struct
 * or the library will no longer be binary compatable with the kernel 
 * plugin and instability will occur.
 */
typedef struct {
	BYTE boardtype;
	WORD base_address;
	BYTE int_level;
	BYTE fifo_enab;
	WORD fifo_depth;
	BYTE flipflop_offset;
	WORD dump_threshold;
	DWORD max_transfers;
	BYTE int_type;
	BYTE high_channel;
	BYTE low_channel;
	BYTE scan_enab;
	BYTE user_int_mode;
	BYTE cycle_mode;
} DSCUDKP_CONF;

#define IOCTL_DSCUDKP_CONF			0x6b01
#define IOCTL_DSCUDKP_DATA			0x6b02
#define IOCTL_DSCUDKP_GET_TRANSFERS	0x6b03
#define IOCTL_DSCUDKP_GET_OPTOSTATE	0x6b04
#define IOCTL_DSCUDKP_GET_VERSION	0x6b05
#define IOCTL_DSCUDKP_FREE_IRQ		0x6b06
#define IOCTL_DSCUDKP_PCI_LIST		0x6b07

#define DSC_PCI_MAX_BOARDS 16
typedef struct {
	int port;
	BYTE irq;
	BYTE slot;
	WORD device_id;
	BYTE boardtype;
} DSCUDKP_PCI_INFO;


/* 
 * This is an outline of how the dscud library and dscudkp.o kernel module work
 * together to read sample data.  It is provided as an introduction to
 * customers curious how the driver works internally.
 *
 * 1. CREATE THE DEVICE (See load.sh)
 *
 * bash% mknod /dev/dsc c 252 0
 *
 * 2. LOAD THE KERNEL MODULE (See load.sh)
 *
 * bash% insmod dscudkp.o
 *
 * 3. TELL THE KERNEL MODULE ABOUT THE BOARD (libdscud5.a internal)
 *
 * DSCUDKP_CONF kpdata;
 * kpdata.boardtype = DSC_DMM32;
 * [initialize rest of struct...]
 * 
 * fd = open("/dev/dscud", "r");
 * ioctl(fd, IOCTL_DSCUDKP_CONF, &kpdata);
 *
 * 4. ENABLE INTERRUPT OPERATIONS ON THE BOARD (libdscud5.a internal)
 *
 * 5. PROCESS SAMPLE DATA FROM THE KERNEL (libdscud5.a internal)
 *
 * while (read(fd, sample_buf, kpdata.dump_threshold)) {
 *     [process interrupt data...]
 * }
 * close(fd);
 *
 * [read returns < 0 in case of error...]
 *
 */

#endif
