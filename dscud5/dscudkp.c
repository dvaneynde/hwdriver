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

/*
 * DSCUDKP
 *
 * A module for high speed interrupt processing for all Diamond Systems 
 * boards.  The primary purpose of the module is to read data from the
 * boards as quickly as possible and transfer this this data in larger
 * buffers to usermode.  It also supports user interrupts such as timers.
 *
 * To use this module first you must create the device:
 *
 *     mknod /dev/dscud c 252 0
 *
 * If the device major 252 is in use, you can select any available number.
 * To load the module you run:
 *
 *     insmod dscudkp.o
 *
 * If you selected a major number other than 252 such as 250 you'll need to run
 * insmod with an additional parameter to use it:
 *
 *     insmod dscudkp.o dscud_major=250
 *
 * The module will now be ready to service interrupts.  The rest of the 
 * Universal Driver (libdscud5.a) interacts with it using a method like
 * the code below.  You as the user of this driver do not need to write
 * code like this since it is all internal to the DSCUD API.  It is only 
 * given as an example to help document how this kernel module works.
 *
 *     int fd;
 *     DSCUDKP_CONF kpconf;
 *
 *     fd = open("/dev/dscud", "r");
 *
 *     kpconf.boardtype = DSC_DMM32;  // initialize all of struct
 *     ioctl(fd, IOCTL_DSCUDKP_CONF, &kpconf);
 *
 *     while ( read(fd, mybuf, bufsize) ) {
 *         //process the data;
 *     }
 *
 *     close(fd); 
 *
 * The driver configures the kernel module with board information using the
 * ioctl() call.  See dscudkp.h for information on this struct.
 *
 */
#ifndef __KERNEL__
#define __KERNEL__
#endif
#ifndef MODULE
#define MODULE
#endif

#ifdef CONFIG_SMP
#define __SMP__
#endif

#include <linux/module.h>
#include <linux/wait.h>
#include <linux/pci.h>
#include <linux/delay.h>
#include <linux/time.h>
#include <asm/io.h>

/*
 * Kernel abstraction code taken with permission from the book "Linux Device
 * Drivers" by Alessandro Rubini and Jonathan Corbet, published by O'Reilly &
 * Associates.
 */

#ifndef CONFIG_PCI
#  error "This driver requires PCI support in the kernel."
#endif

#ifndef LINUX_VERSION_CODE
#  include <linux/version.h>
#endif

#ifndef KERNEL_VERSION /* pre-2.1.90 didn't have it */
#  define KERNEL_VERSION(vers,rel,seq) ( ((vers)<<16) | ((rel)<<8) | (seq) )
#endif

#if LINUX_VERSION_CODE < KERNEL_VERSION(2,0,0) /* not < 2.0 */
#  error "This kernel is too old: not supported by this file"
#endif
#if (LINUX_VERSION_CODE & 0xff00) == 5 /* not 2.5 */
#  error "Kernel version 2.5 not supported by this file"
#endif
#if (LINUX_VERSION_CODE & 0xff00) == 1 /* not 2.1 */
#  error "Please don't use linux-2.1, use 2.2 or 2.4 instead"
#endif
#if (LINUX_VERSION_CODE & 0xff00) == 3 /* not 2.3 */
#  error "Please don't use linux-2.3, use 2.4 instead"
#endif

/* remember about the current version */
#if LINUX_VERSION_CODE < KERNEL_VERSION(2,1,0)
#  define LINUX_20
#elif LINUX_VERSION_CODE < KERNEL_VERSION(2,3,0)
#  define LINUX_22
#elif LINUX_VERSION_CODE < KERNEL_VERSION(2, 5, 0)
#  define LINUX_24
#elif LINUX_VERSION_CODE < KERNEL_VERSION(2, 7, 0)
#  define LINUX_26
#else 
#  error "Kernel version not supported"
#endif

#ifdef LINUX_20
#  define EXPORT_NO_SYMBOLS register_symtab(NULL);
#  define MODULE_PARM(v,t)           /* nothing */
#endif

#ifndef SET_MODULE_OWNER
#  define SET_MODULE_OWNER(structure) /* nothing */
#endif

/*
 * Wait queues changed with 2.3
 */
#ifndef DECLARE_WAIT_QUEUE_HEAD
#  define DECLARE_WAIT_QUEUE_HEAD(head) struct wait_queue *head = NULL
   typedef  struct wait_queue *wait_queue_head_t;
#  define init_waitqueue_head(head) (*(head)) = NULL
#endif

#ifdef LINUX_20
#  include <asm/segment.h>
#  define   copy_to_user(t,f,n)         (memcpy_tofs((t), (f), (n)), 0)
#  define copy_from_user(t,f,n)       (memcpy_fromfs((t), (f), (n)), 0)
#else
#  include <asm/uaccess.h>
#endif

#ifdef LINUX_20
#  define __USE_OLD_REBUILD_HEADER__
#endif

#ifdef LINUX_24
#	include <linux/slab.h>
#elif LINUX_22
#	include <linux/malloc.h>
#endif

#ifdef LINUX_26
#   include <linux/fs.h>
#   include <linux/interrupt.h>
#endif

#ifdef LINUX_26
	typedef int isr_return_t;
	#define ISR_SKIPPED 0
	#define ISR_HANDLED 1
#else
	typedef void isr_return_t;
	#define ISR_SKIPPED
	#define ISR_HANDLED
#endif

/* END KERNEL ABSTRACTION DEFINITIONS */

#ifdef MODULE_AUTHOR
MODULE_AUTHOR("Diamond Systems, Inc");
#endif

#ifdef MODULE_DESCRIPTION
MODULE_DESCRIPTION("Fast interrupt support for Diamond Systems DAQ boards");
#endif

#ifdef MODULE_SUPPORTED_DEVICE
MODULE_SUPPORTED_DEVICE("/dev/dscud");
#endif

#ifdef MODULE_LICENSE
MODULE_LICENSE("Dual BSD/GPL");
#endif

#include "dscudkp.h"

/* Timeout when waiting for status bit in AD interrupt scan for dmm+dmm16
 * and DA interrupt for dmm32+hercules */
#define TIMEOUT_MICROSECS 100

/* 
 * Default device major number is 252.  In the unlikely event that this 
 * number is already in use, you can overide this number at kernel module
 * load time like this:
 *
 * bash% insmod dscudkp dscud_major=201
 *
 */
static int dscud_major = 252;
MODULE_PARM(dscud_major, "i");

static DSCUDKP_PCI_INFO dsc_pci_boards[DSC_PCI_MAX_BOARDS];

typedef struct {
	/* Board configuration from DSCUDKP_CONF */
	BYTE boardtype;
	WORD base_address;
	BYTE int_level;
	BYTE flipflop_offset;
	WORD dump_threshold;
	DWORD max_transfers;
	BYTE int_type;
	BYTE high_channel;
	BYTE low_channel;
	BYTE scan_enab;
	BYTE user_int_mode;
	BYTE cycle_mode;

	/*
	 * Queue used to wakup read() operations when another dump_threshold
	 * chunk of data is ready to be transfered to usermode.
	 */
	wait_queue_head_t read_queue;

	/*  Flag determines if module will free IRQ on close() */
	int irq_active;

	/* Buffer variables */
	SWORD *samples;  /* max_transfers sized */
	SWORD *pCurPos;  /* Pointer to current position in *samples */

	/* Read copy variables */
	SWORD *pReadPos; /* Pointer to next copy to read() */
	DWORD read_transfers; /* Number of transfers copied to read() */

	/* Number of samples to read from fifo.  high_ch - low_ch or fifo depth */
	WORD samples_per_int;

	/* For calulating how often to transfer data to user mode */
	DWORD cur_transfers;
	DWORD total_transfers;

	/* For boards which require software based channel incrementing */
	WORD cur_channel;

	/* Error code if a failure happened.  Uses dscud.h errors */
	BYTE error;

	/* Last DMM48 opto state. Saved in case usermode requests it */
	BYTE dmm48_opto_state;

	/* Port and value to use for checking that the board has raised
	 * the interrupt for this operation.  Required for IRQ sharing. 
	 * Not used for DA interrupts */
	BYTE int_status_port;
	BYTE int_status_val;

	/* Value to use for resetting the interrupt line. Not used for 
	 * DA interrupts. */
	BYTE flipflop_reset_val;

	/* For wakeup sync between ISR and dscud_read() */
	DWORD event_counter, event_counter_saved;

} KP_DATA;

#define DSC_PCI_VENDOR_ID 0x11db   /* Temporary development ID */
/* #define DSC_PCI_VENDOR_ID 0x100b */       /* National Semiconductor */

/*
 *  This is a list which maps PCI device IDs to DSCUD board types.
 *  Useed for scanning the system for Diamond Systems PCI devices
*/
struct
{
    WORD device_id;         /* PCI device ID */
    BYTE boardtype;         /* DSCUD board type */
} DSC_PCI_DEVICES[] = {
    {0x0020, DSC_METIS},
    {0, 0}
};



/*
 * cleanup_module() - Release module resources
 */
void cleanup_module(void)
{
	unregister_chrdev(dscud_major, "dscud");
}

/*
 * dscud_open() - Handle open(/dev/dscud)
 */
static int dscud_open(struct inode *node, struct file *filep)
{
	KP_DATA *kpdata;

	kpdata = (KP_DATA *)vmalloc(sizeof(KP_DATA));
	if ( kpdata == NULL ) {
		printk("dscudkp: dscud_open(): vmalloc of %d bytes failed\n", sizeof(KP_DATA));
		return -ENOMEM;
	}

	memset(kpdata, 0, sizeof(KP_DATA));
	init_waitqueue_head (&(kpdata->read_queue));

	filep->private_data = (void *)kpdata;

#ifndef LINUX_26
	MOD_INC_USE_COUNT; 
#endif

	return 0;		
}

/*
 * dscud_release() - Handle close(/dev/dscud).  Free board resources.
 */
static int dscud_release(struct inode *node, struct file *filep)
{
	KP_DATA *kpdata = (KP_DATA *)filep->private_data;

	if ( kpdata->irq_active )
		free_irq(kpdata->int_level, kpdata);

	if ( kpdata->samples != NULL ) {
		vfree(kpdata->samples);
		kpdata->samples = NULL;
	}

	vfree(kpdata);
	filep->private_data = NULL;

#ifndef LINUX_26
	MOD_DEC_USE_COUNT;
#endif

	return 0;		
}

/*
 * dscud_read() - Handle read(/dev/dscud). Wait for ISR to signal that another
 * buffer is ready for transfer to user mode
 */
static ssize_t dscud_read(struct file *filep, char *buff, size_t count, 
	loff_t *offp)
{
	KP_DATA *kpdata = (KP_DATA *)filep->private_data;
	int rc;
	int n;

	if ( kpdata->int_type != INT_TYPE_AD || count < 1) {
		interruptible_sleep_on(&kpdata->read_queue);
		if ( signal_pending(current) )
			return -ERESTARTSYS;
		return 0;
	}

	/* Wait for next chunk of data from the ISR */
	wait_event_interruptible(kpdata->read_queue, 
			(kpdata->read_transfers != kpdata->cur_transfers) 
			|| (kpdata->event_counter != kpdata->event_counter_saved) );

	kpdata->event_counter_saved = kpdata->event_counter;

	if ( signal_pending(current) )
		return -ERESTARTSYS;

	/* Error occured in ISR.  Right now this only happens if there is a
	 * timeout while waiting for a status bit */
	if ( kpdata->error )
		return -(kpdata->error);

	/* Calculate how many bytes to copy to read() from pReadPos */
	if ( kpdata->read_transfers < kpdata->cur_transfers ) {
		n = kpdata->cur_transfers - kpdata->read_transfers;
	} else {
		n = kpdata->max_transfers - kpdata->read_transfers;
	}

	n *= sizeof(SWORD);

	/* This number should not be greater than the data available */
	if ( count > n )
		count = n;

	rc = copy_to_user(buff, kpdata->pReadPos, count);
	if ( rc ) {
		printk("dscudkp: dscud_read(): copy_to_user() failed with rc=%d\n", rc);
		return -EFAULT;
	}

	kpdata->read_transfers += (count / sizeof(SWORD));
	kpdata->pReadPos += (count / sizeof(SWORD));
	if ( kpdata->read_transfers >= kpdata->max_transfers ) {
		kpdata->read_transfers = 0;
		kpdata->pReadPos = kpdata->samples;
	}

	/* Return to usermode */
	return count;
}

/*
 * busy_wait_status() - implements a function for waiting on a hw status
 * bit to change with a short microsecond timeout.  
 */
int busy_wait_status(int addr, unsigned char reg)
{
	int ticks;

	for ( ticks = 0; (inb(addr) & reg) && ticks < TIMEOUT_MICROSECS; ticks++ )
		udelay(1);

	if ( ticks >= TIMEOUT_MICROSECS ) {
		return ETIME;
	}

	return 0;
}

/* 
 * at_adint() - interrupt processing routine for all FIFO equiped DMM* boards
 * plus the Prometheus and Hercules.  Any Diamond board which fits the basic
 * model of insw(baseaddr, fifo), outb(value, flipflop) can work with this
 * routine.
 */
static isr_return_t at_adint(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;
	int i;

	if ( !(inb(kpdata->base_address + kpdata->int_status_port) & 
			kpdata->int_status_val ) )
		return ISR_SKIPPED;

	/* NOTE: WORKAROUND FOR OLD PRZ BUG
	 * Supress spurious PRZ interrupts when fifo not at correct depth.
	 */
	if ( kpdata->boardtype == DSC_PROM ) {
		BYTE fifo_thresh = inb(kpdata->base_address+5);
		BYTE fifo_depth = inb(kpdata->base_address+6);

		if ( fifo_thresh == 0 )
			fifo_thresh = 1;

		if ( fifo_depth < fifo_thresh ) {
			outb(0x01, kpdata->base_address + kpdata->flipflop_offset);
			return ISR_HANDLED;
		}
	}

	/* Drain the fifo */
	insw(kpdata->base_address, kpdata->pCurPos, kpdata->samples_per_int);

	/* DMMAT only does 12 bit samples */
	if ( kpdata->boardtype == DSC_DMMAT )
		for ( i = 0; i < kpdata->samples_per_int; i++ )
			*(kpdata->pCurPos + i) = (*(kpdata->pCurPos + i) >> 4) & 0x0FFF;

	/* Advance our buffer pointers */
	kpdata->pCurPos += kpdata->samples_per_int;
	kpdata->cur_transfers += kpdata->samples_per_int;
	kpdata->total_transfers += kpdata->samples_per_int;

	if ( kpdata->cur_transfers >= kpdata->max_transfers ) {
		kpdata->pCurPos = kpdata->samples;
		kpdata->cur_transfers = 0;
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

		if ( !kpdata->cycle_mode )
			return ISR_HANDLED;
	} else if ( kpdata->dump_threshold && 
		!(kpdata->cur_transfers % kpdata->dump_threshold) )
	{
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

	}

	outb(kpdata->flipflop_reset_val, kpdata->base_address + kpdata->flipflop_offset);
	return ISR_HANDLED;
}


/* 
 * nonat_adint_sample() - Handle sample interrupt for DMM.
 */
static isr_return_t nonat_adint_sample(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;

	/* Drain the fifo */
	*kpdata->pCurPos = inw(kpdata->base_address);
	*kpdata->pCurPos = (*kpdata->pCurPos >> 4) & 0x0FFF;

	/* Advance our buffer pointers */
	kpdata->pCurPos++;
	kpdata->cur_transfers++;

	/* Set new channel only if we are sampling on multiple channels */
	if ( kpdata->low_channel != kpdata->high_channel ) {

		kpdata->cur_channel++;
		if ( kpdata->cur_channel > kpdata->high_channel )
			kpdata->cur_channel = kpdata->low_channel;

		outb((BYTE)((kpdata->high_channel << 4) | kpdata->cur_channel) , 
			kpdata->base_address + 2);

		/* Wait for the status bit */
		if ( busy_wait_status(kpdata->base_address + 11, 0x10) ) {
			printk("dscudkp: nonat_adint_sample(): busy_wait_status() timed out\n");
			kpdata->error = ETIME;
			kpdata->event_counter++; 
			wake_up(&kpdata->read_queue);  // Wakes up read()
			return ISR_HANDLED;
		}
	}

	if ( kpdata->cur_transfers >= kpdata->max_transfers ) {
		kpdata->pCurPos = kpdata->samples;
		kpdata->cur_transfers = 0;
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

		if ( !kpdata->cycle_mode )
			return ISR_HANDLED;
	} else if ( kpdata->dump_threshold && 
		!(kpdata->cur_transfers % kpdata->dump_threshold) )
	{
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

	}

	/* Reset the flipflop and wait for the next interrupt */
	outb(0x08, kpdata->base_address + kpdata->flipflop_offset);

	return ISR_HANDLED;
}


/* 
 * dmm_adint_scan() - Handle scan interrupts for the DMM.  These
 * boards do not directly support scan interrupts so channel selection is
 * driven by software.
 */
static isr_return_t nonat_adint_scan(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;
	int tmp;
	int ch;

	tmp = inb(kpdata->base_address + 8);

	/* Interrupt sharing not supported for these boards */
	if ( !(tmp & 0x10 ) )
		return ISR_SKIPPED;

	/* Disable interrupts */
	tmp = inb(kpdata->base_address + 9);
	outb((BYTE)(tmp & 0x7D) , kpdata->base_address + 9);

	/* Read the first sample */
	*kpdata->pCurPos = inw(kpdata->base_address);
	*kpdata->pCurPos = (*kpdata->pCurPos >> 4) & 0x0FFF;

	/* Advance our buffer pointers */
	kpdata->pCurPos++;
	kpdata->cur_transfers++;

	/* Trigger and read samples for the other channels.  Note the wait
	 * for the status bit, which may cause system problems on hardware failure */
	for ( ch = kpdata->low_channel + 1; ch <= kpdata->high_channel; ch++ ) {

		/* Trigger another conversion */
		outb(0xFF, kpdata->base_address);

		/* Wait for the status bit */
		if ( busy_wait_status(kpdata->base_address + 8, 0x80) ) {
			printk("dscudkp: nonat_adint_scan(): busy_wait_status() timed out\n");
			kpdata->error = ETIME;
			kpdata->event_counter++; 
			wake_up(&kpdata->read_queue);  // Wakes up read()
			return ISR_HANDLED;
		}

		/* Read the sample */
		*kpdata->pCurPos = inw(kpdata->base_address);
		*kpdata->pCurPos = (*kpdata->pCurPos >> 4) & 0x0FFF;

		kpdata->pCurPos++;
		kpdata->cur_transfers++;
	}

	/* Reset the channel */
	outb((BYTE)((kpdata->high_channel << 4) | kpdata->low_channel) , 
					kpdata->base_address + 2);

	/* Wait for the status bit */
	if ( busy_wait_status(kpdata->base_address + 11, 0x10) ) {
		printk("dscudkp: nonat_adint_scan(): busy_wait_status() timed out\n");
		kpdata->error = ETIME;
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()
		return ISR_HANDLED;
	}

	if ( kpdata->cur_transfers >= kpdata->max_transfers ) {
		tmp = inb(kpdata->base_address + 9);
		outb((BYTE)(tmp & 0xFD) , kpdata->base_address + 9);
		kpdata->pCurPos = kpdata->samples;
		kpdata->cur_transfers = 0;
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

		if ( !kpdata->cycle_mode )
			return ISR_HANDLED;
	} else if ( kpdata->dump_threshold && 
		!(kpdata->cur_transfers % kpdata->dump_threshold) )
	{
		tmp = inb(kpdata->base_address + 9);
		outb((BYTE)(tmp & 0xFD) , kpdata->base_address + 9);
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

	}

	/* Reset the flipflop and wait for the next interrupt */
	tmp = inb(kpdata->base_address + 9);
	outb(0x08, kpdata->base_address + kpdata->flipflop_offset);
	outb((BYTE)(tmp | 0x82) , kpdata->base_address + 9);

	return ISR_HANDLED;
}


/* 
 * zmm_adint_sample() - Handle sample interrupts for the ZMM.
 */
static isr_return_t zmm_adint_sample(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;
	BYTE config;

	/* Interrupt sharing not supported for these boards */

	/* Read the sample */
	*kpdata->pCurPos = inb(kpdata->base_address);

	/* Advance our buffer pointers */
	kpdata->pCurPos += kpdata->samples_per_int;
	kpdata->cur_transfers += kpdata->samples_per_int;

	/* Maintain the current channel variable */
	if ( ++kpdata->cur_channel > kpdata->high_channel )
		kpdata->cur_channel = kpdata->low_channel;

	/* Read config */
	config = inb(kpdata->base_address + 4);

	/* Increment the channel */
	outb(kpdata->cur_channel | config, kpdata->base_address + 4);

	if ( kpdata->cur_transfers >= kpdata->max_transfers ) {
		kpdata->pCurPos = kpdata->samples;
		kpdata->cur_transfers = 0;
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

		if ( !kpdata->cycle_mode )
			return ISR_HANDLED;
	} else if ( kpdata->dump_threshold && 
		!(kpdata->cur_transfers % kpdata->dump_threshold) )
	{
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

	}

	/* Reset the flipflop and wait for the next interrupt */
	inb(kpdata->base_address + kpdata->flipflop_offset);

	return ISR_HANDLED;
}

/* 
 * zmm_adint_scan() - Handle scan interrupts for the ZMM.
 */
static isr_return_t zmm_adint_scan(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;
	BYTE config;
	int ch;

	/* Interrupt sharing not supported for these boards */

	/* Read the sample */
	*kpdata->pCurPos = inb(kpdata->base_address);

	/* Advance our buffer pointers */
	kpdata->pCurPos += kpdata->samples_per_int;
	kpdata->cur_transfers += kpdata->samples_per_int;

	/* Maintain the current channel variable */
	if ( ++kpdata->cur_channel > kpdata->high_channel )
		kpdata->cur_channel = kpdata->low_channel;

	/* Read config */
	config = inb(kpdata->base_address + 4);

	/* Increment the channel */
	outb(kpdata->cur_channel | config, kpdata->base_address + 4);

	/* Trigger and read samples for the other channels.  Note the wait
	 * for the status bit, which will lock up the system */
	for ( ch = kpdata->low_channel + 1; ch <= kpdata->high_channel; ch++ ) {
		outb(0xFF, kpdata->base_address + 2);   // Trigger another conversion

		/* Wait for the status bit */
		if ( busy_wait_status(kpdata->base_address + 4, 0x80) ) {
			printk("dscudkp: zmm_adint_scan(): busy_wait_status() timed out\n");
			kpdata->error = ETIME;
			kpdata->event_counter++; 
			wake_up(&kpdata->read_queue);  // Wakes up read()
			return ISR_HANDLED;
		}

		/* Read the sample */
		*kpdata->pCurPos = inw(kpdata->base_address);
		kpdata->pCurPos++;
		kpdata->cur_transfers++;
	
		/* Maintain the current channel variable */
		if ( ++kpdata->cur_channel > kpdata->high_channel )
			kpdata->cur_channel = kpdata->low_channel;

		outb(kpdata->cur_channel | config, kpdata->base_address + 4);
	}

	if ( kpdata->cur_transfers >= kpdata->max_transfers ) {
		kpdata->pCurPos = kpdata->samples;
		kpdata->cur_transfers = 0;
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

		if ( !kpdata->cycle_mode )
			return ISR_HANDLED;
	} else if ( kpdata->dump_threshold && 
		!(kpdata->cur_transfers % kpdata->dump_threshold) )
	{
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

	}

	/* Reset the flipflop and wait for the next interrupt */
	inb(kpdata->base_address + kpdata->flipflop_offset);

	return ISR_HANDLED;
}

/* 
 * at_userint() - Handle user interrupts for all AT boards.  These are timers
 * which require no kernel processing or data transfer.  A routine in usermode
 * will be run.
 */
static isr_return_t at_userint(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;

	if ( !(inb(kpdata->base_address + kpdata->int_status_port)
				& kpdata->int_status_val) ) 
		return ISR_SKIPPED;

	/* Wakes up read() which calls usermode user interrupt function */
	kpdata->event_counter++;
	wake_up(&kpdata->read_queue);

	if ( DSC_DMM48 == kpdata->boardtype )
		kpdata->dmm48_opto_state = inb(kpdata->base_address+7);

	outb(kpdata->flipflop_reset_val, kpdata->base_address 
		+ kpdata->flipflop_offset);

	return ISR_HANDLED;
}


/* 
 * nonat_userint() - Handle user interrupts for all boards.  These are timers
 * which require no kernel processing or data transfer.  A routine in usermode
 * will be run.
 */
static isr_return_t nonat_userint(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;
	BYTE tmp;

	/* Ignore interrupt if the correct xINT register is not set.
	 * Otherwise reset the flipflop */
	switch (kpdata->boardtype) {
		case DSC_DMM:
			tmp = inb(kpdata->base_address + 9);
			outb(0x08, kpdata->base_address + kpdata->flipflop_offset);
			outb((BYTE)(tmp | 0x82) , kpdata->base_address + 9);

		case DSC_QMM:
			inb(kpdata->base_address + 7);

		case DSC_ZMM:
			inb(kpdata->base_address + kpdata->flipflop_offset);
	}

	/* Wakes up read() which calls usermode user interrupt function */
	kpdata->event_counter++;
	wake_up(&kpdata->read_queue);

	return ISR_HANDLED;
}

/*
 * generic_daint() - D/A interrupt support.  See the universal driver
 * user manual for more information.  A buffer is passed from usermode
 * which contains DA codes which are output on a range of channels at
 * a rate specified by the counter or by the AD clock if a counter is
 * not present.
 */
static isr_return_t generic_daint(int irq, void *dev_id, struct pt_regs *regs)
{
	KP_DATA *kpdata = (KP_DATA *)dev_id;
	BYTE ch, lsb, msb;
	int tmp;

	/* Ignore interrupt if TINT (or INT for DMM/ZMM) isn't set. */
	switch (kpdata->boardtype) {
	case DSC_DMM32XAT:
		case DSC_DMM32:
			if ( !(inb(kpdata->base_address + 9) & 0x20) ) {
				return ISR_SKIPPED;
			}
			break;

		case DSC_PROM:
		case DSC_ATHENA:
		case DSC_ELEKTRA:
			if ( !(inb(kpdata->base_address + 7) & 0x40) )
				return ISR_SKIPPED;
			break;

		case DSC_DMM16AT:
		case DSC_DMMAT:
			if ( !(inb(kpdata->base_address + 8) & 0x40) )
				return ISR_SKIPPED;
			break;

		case DSC_HERCEBX:
			if ( !(inb(kpdata->base_address + 14) & 0x40) ) 
				return ISR_SKIPPED;
			break;

		case DSC_DMM48:
			if ( !(inb(kpdata->base_address + 11) & 0x80) )
				return ISR_SKIPPED;

			break;
	}

	for ( ch = kpdata->low_channel; ch <= kpdata->high_channel; ch++ ) {
		if ( kpdata->boardtype == DSC_DMM32 || kpdata->boardtype == DSC_DMM32XAT) {
			lsb = *kpdata->pCurPos & 0xFF;
			msb = ((*kpdata->pCurPos >> 8) & 0x0F) | (ch<<6);

			outb(lsb, kpdata->base_address + 4);

			/* Wait for the status bit */
			if ( busy_wait_status(kpdata->base_address + 4, 0x80) ) {
				printk("dscudkp: generic_daint(): busy_wait_status() timed out\n");
				kpdata->error = ETIME;
				kpdata->event_counter++; 
				wake_up(&kpdata->read_queue);  // Wakes up read()
				return ISR_HANDLED;
			}

			/* Send the MSB */
			outb(msb, kpdata->base_address + 5);

			/* Wait for the status bit */
			if ( busy_wait_status(kpdata->base_address + 4, 0x80) ) {
				printk("dscudkp: generic_daint(): busy_wait_status() timed out\n");
				kpdata->error = ETIME;
				kpdata->event_counter++; 
				wake_up(&kpdata->read_queue);  // Wakes up read()
				return ISR_HANDLED;
			}

		} else if ( kpdata->boardtype == DSC_DMM16AT ) {
			lsb = *kpdata->pCurPos & 0xFF;
			msb = (*kpdata->pCurPos >> 8) & 0x0F;

			outb(lsb, kpdata->base_address + 1);
			outb(msb, kpdata->base_address + 4 + ch);

		} else if ( kpdata->boardtype == DSC_DMMAT || 
						kpdata->boardtype == DSC_DMM ) 
		{
			outw(*kpdata->pCurPos, kpdata->base_address + 4 + (ch*2));

		} else if ( kpdata->boardtype == DSC_PROM || 
				kpdata->boardtype == DSC_ATHENA || 
				kpdata->boardtype == DSC_ELEKTRA ) 
		{
			lsb = *kpdata->pCurPos & 0xFF;
			msb = ((*kpdata->pCurPos >> 8) & 0x0F) | (ch<<6);

			outb(lsb, kpdata->base_address + 6);
			outb(msb, kpdata->base_address + 7);

			/* Wait for the status bit */
			if ( busy_wait_status(kpdata->base_address + 3, 0x10) ) {
				printk("dscudkp: generic_daint(): busy_wait_status() timed out\n");
				kpdata->error = ETIME;
				kpdata->event_counter++; 
				wake_up(&kpdata->read_queue);  // Wakes up read()
				return ISR_HANDLED;
			}

		} else if ( kpdata->boardtype == DSC_DMM48 ) {
			lsb = *kpdata->pCurPos & 0xFF;
			msb = ((*kpdata->pCurPos >> 8) & 0xFF);

			outb(lsb, kpdata->base_address);
			outb(msb, kpdata->base_address + 1);
			outb(ch, kpdata->base_address + 7);

			/* Wait for the status bit */
			if ( busy_wait_status(kpdata->base_address + 9, 0x40) ) {
				printk("dscudkp: generic_daint(): busy_wait_status() timed out\n");
				kpdata->error = ETIME;
				kpdata->event_counter++; 
				wake_up(&kpdata->read_queue);  // Wakes up read()
				return ISR_HANDLED;
			}

			outb(0x08, kpdata->base_address + 7);

		} else if ( kpdata->boardtype == DSC_HERCEBX ) {
			lsb = *kpdata->pCurPos & 0xFF;
			msb = ((*kpdata->pCurPos >> 8) & 0x0F) | (ch<<6);

			outb(lsb, kpdata->base_address + 6);
			outb(msb, kpdata->base_address + 7);
			outb(ch, kpdata->base_address + 5);

			/* Wait for the status bit */
			if ( busy_wait_status(kpdata->base_address + 4, 0x20) ) {
				printk("dscudkp: generic_daint(): busy_wait_status() timed out\n");
				kpdata->error = ETIME;
				kpdata->event_counter++; 
				wake_up(&kpdata->read_queue);  // Wakes up read()
				return ISR_HANDLED;
			}

		} else if ( kpdata->boardtype == DSC_ZMM ) {
			msb = (BYTE)*kpdata->pCurPos;
			outb(msb, kpdata->base_address);
		}

		kpdata->pCurPos++;
	}

	/* Triggers the DA conversion */
	if ( kpdata->boardtype == DSC_DMM16AT )
		inb(kpdata->base_address + 4);

	kpdata->cur_transfers += kpdata->samples_per_int;

	if ( kpdata->cur_transfers >= kpdata->max_transfers ) {
		kpdata->cur_transfers = 0;
		kpdata->pCurPos = kpdata->samples;
		kpdata->event_counter++; 
		wake_up(&kpdata->read_queue);  // Wakes up read()

		if ( !kpdata->cycle_mode )
			return ISR_HANDLED;
	}
	
	/* Reset the flipflop */
	switch (kpdata->boardtype) {
		case DSC_DMM32:
		case DSC_DMM32XAT:
		case DSC_DMM16AT:
		case DSC_DMMAT:
		case DSC_HERCEBX:
			outb(0x08, kpdata->base_address + kpdata->flipflop_offset);
			break;

		case DSC_DMM48:
			outb(0x80, kpdata->base_address + kpdata->flipflop_offset);
			break;

		case DSC_PROM:
		case DSC_ATHENA:
		case DSC_ELEKTRA:
			outb(0x04, kpdata->base_address + kpdata->flipflop_offset);
			break;

		case DSC_ZMM:
			inb(kpdata->base_address + kpdata->flipflop_offset);
			break;

		case DSC_DMM:
			tmp = inb(kpdata->base_address + 9);
			outb(0x08, kpdata->base_address + kpdata->flipflop_offset);
			outb((BYTE)(tmp | 0x82) , kpdata->base_address + 9);
			break;
	}

	return ISR_HANDLED;
}


/*
 * initialize_userint() - Process configuration from ioctl() for a user interrupt.
 */
static int initialize_userint(KP_DATA *kpdata, DSCUDKP_CONF *conf)
{
	int rc;

	kpdata->boardtype = conf->boardtype;
	kpdata->base_address = conf->base_address;
	kpdata->int_level = conf->int_level;
	kpdata->flipflop_offset = conf->flipflop_offset;
	kpdata->dump_threshold = conf->dump_threshold;
	kpdata->max_transfers = conf->max_transfers;
	kpdata->int_type = conf->int_type;
	kpdata->high_channel = conf->high_channel;
	kpdata->low_channel = conf->low_channel;
	kpdata->scan_enab = conf->scan_enab;
	kpdata->user_int_mode = conf->user_int_mode;

	switch (kpdata->boardtype) {
		case DSC_DMM32:
		case DSC_DMM32XAT:
			if ( kpdata->int_type == INT_TYPE_COUNTER 
							|| kpdata->int_type == INT_TYPE_DIOIN ) {
				kpdata->int_status_port = 9;
				kpdata->int_status_val = 0x60;
			} else {  /* INT_TYPE_AD */
				kpdata->int_status_port = 9;
				kpdata->int_status_val = 0x80;
			}

			kpdata->flipflop_reset_val = 0x08;
			break;

		case DSC_PROM:
		case DSC_ATHENA:
		case DSC_ELEKTRA:
			if ( kpdata->int_type == INT_TYPE_COUNTER || kpdata->int_type == INT_TYPE_USER ) {
				kpdata->int_status_port = 7;
				kpdata->int_status_val = 0x40;
				kpdata->flipflop_reset_val = 0x04;
			} else if ( kpdata->int_type == INT_TYPE_DIOIN ) {
				kpdata->int_status_port = 7;
				kpdata->int_status_val = 0x20;
				kpdata->flipflop_reset_val = 0x02;
			} else { /* INT_TYPE_AD */
				kpdata->int_status_port = 7;
				kpdata->int_status_val = 0x10;
				kpdata->flipflop_reset_val = 0x01;
			}
			break;

		case DSC_DMMAT:
		case DSC_DMM16AT:
			if ( kpdata->int_type == INT_TYPE_COUNTER ) {
				kpdata->int_status_port = 8;
				kpdata->int_status_val = 0x40;
			} else { /* INT_TYPE_AD */
				kpdata->int_status_port = 8;
				kpdata->int_status_val = 0x10;
			}
			kpdata->flipflop_reset_val = 0x08;
			break;

		case DSC_DMM48:
			if ( kpdata->int_type == INT_TYPE_DIOIN ) {
				kpdata->int_status_port = 11;
				kpdata->int_status_val = 0x40;
				kpdata->flipflop_reset_val = 0x40;
			} else if ( kpdata->int_type == INT_TYPE_COUNTER) {
				kpdata->int_status_port = 11;
				kpdata->int_status_val = 0x80;
				kpdata->flipflop_reset_val = 0x80;
			} else if ( kpdata->int_type == INT_TYPE_OPTO ) {
				kpdata->int_status_port = 11;
				kpdata->int_status_val = 0x20;
				kpdata->flipflop_reset_val = 0x20;
			} else {  /* INT_TYPE_AD */
				kpdata->int_status_port = 11;
				kpdata->int_status_val = 0x10;
				kpdata->flipflop_reset_val = 0x10;
			}
			break;

		case DSC_HERCEBX:
			if ( kpdata->int_type == INT_TYPE_COUNTER ) {
				kpdata->int_status_port = 14;
				kpdata->int_status_val = 0x40;
				kpdata->flipflop_reset_val = 0x08;
			} else if ( kpdata->int_type == INT_TYPE_DIOIN ) {
				kpdata->int_status_port = 14;
				kpdata->int_status_val = 0x20;
				kpdata->flipflop_reset_val = 0x04;
			} else { /* INT_TYPE_AD */
				kpdata->int_status_port = 14;
				kpdata->int_status_val = 0x10;
				kpdata->flipflop_reset_val = 0x02;
			}
			break;
	}

	switch (kpdata->boardtype) {
		case DSC_DMM48:
		case DSC_DMM32:
		case DSC_DMM32XAT:
		case DSC_PROM:
		case DSC_ATHENA:
		case DSC_ELEKTRA:
		case DSC_DMM16AT:
		case DSC_DMMAT:
		case DSC_HERCEBX:
			rc = request_irq(kpdata->int_level, &at_userint, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			break;

		default:
			rc = request_irq(kpdata->int_level, &nonat_userint, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			break;
	}

	if ( rc ) {
		printk("dscudkp: initialize_userint(): request_irq() for irq %d failed with rc=%d\n", kpdata->int_level, rc);
		return rc;
	}

	kpdata->irq_active = 1;
	return 0;
}


/*
 * initialize_adint() - Process configuration struct from ioctl() and make
 * sure the configuration is valid.  Register an interrupt handler.
 */
static int initialize_adint(KP_DATA *kpdata, DSCUDKP_CONF *conf)
{
	int rc;

	/* Copy to our kernel plugin specific struct */
	kpdata->boardtype = conf->boardtype;
	kpdata->base_address = conf->base_address;
	kpdata->int_level = conf->int_level;
	kpdata->flipflop_offset = conf->flipflop_offset;
	kpdata->dump_threshold = conf->dump_threshold;
	kpdata->max_transfers = conf->max_transfers;
	kpdata->int_type = conf->int_type;
	kpdata->high_channel = conf->high_channel;
	kpdata->low_channel = conf->low_channel;
	kpdata->scan_enab = conf->scan_enab;
	kpdata->user_int_mode = conf->user_int_mode;
	kpdata->cycle_mode = conf->cycle_mode;

	if ( kpdata->user_int_mode == USER_INT_INSTEAD )
		return initialize_userint(kpdata, conf);

	/* Make sure max_transfers is non zero */
	if ( conf->max_transfers < 1 ) {
		printk("dscudkp: initialize_adint(): max_transfers %ld is invalid\n", kpdata->max_transfers);
		return -EINVAL;
	}

	/* Calculate samples_per_int now to save repetitive caluculations later in
	 * the ISR */
	if ( conf->fifo_enab && conf->fifo_depth > 0 )
		kpdata->samples_per_int = conf->fifo_depth;
	else if ( kpdata->scan_enab )
		kpdata->samples_per_int = 1 + kpdata->high_channel - kpdata->low_channel;
	else
		kpdata->samples_per_int = 1;

	kpdata->samples = vmalloc(kpdata->max_transfers * sizeof(SWORD));
	if ( kpdata->samples == NULL ) {
		printk("dscudkp: initialize_adint(): vmalloc of %ld bytes failed\n", kpdata->max_transfers * sizeof(SWORD));
		return -ENOMEM;
	}

	kpdata->pCurPos = kpdata->samples;
	kpdata->pReadPos = kpdata->samples;
	kpdata->cur_transfers = 0;
	kpdata->irq_active = 1;
	kpdata->cur_channel = kpdata->low_channel;
	rc = -EINVAL;

	if ( kpdata->boardtype == DSC_PROM || kpdata->boardtype == DSC_ATHENA 
			|| kpdata->boardtype == DSC_ELEKTRA ) 
	{
		kpdata->int_status_port = 7;
		kpdata->int_status_val = 0x10;
	} else if ( kpdata->boardtype == DSC_DMM48 ) {
		kpdata->int_status_port = 11;
		kpdata->int_status_val = 0x10;
	} else if ( kpdata->boardtype == DSC_HERCEBX ) {
		kpdata->int_status_port = 14;
		kpdata->int_status_val = 0x10;
	} else if ( kpdata->boardtype == DSC_DMM16AT || 
					kpdata->boardtype == DSC_DMMAT ) 
	{
		kpdata->int_status_port = 8;
		kpdata->int_status_val = 0x10;
	} else {
		kpdata->int_status_port = 9;
		kpdata->int_status_val = 0x80;
	}

	if ( kpdata->boardtype == DSC_PROM || kpdata->boardtype == DSC_ATHENA 
			|| kpdata->boardtype == DSC_ELEKTRA ) 
	{
		kpdata->flipflop_reset_val = 0x01;
	} else if ( kpdata->boardtype == DSC_DMM48 ) {
		kpdata->flipflop_reset_val = 0x10;
	} else if ( kpdata->boardtype == DSC_HERCEBX ) {
		kpdata->flipflop_reset_val = 0x02;
	} else {
		kpdata->flipflop_reset_val = 0x08;
	}

	/* Register an IRQ handler */
	switch (kpdata->boardtype) {
		case DSC_DMM48:
		case DSC_DMM32:
		case DSC_DMM32XAT:
		case DSC_PROM:
		case DSC_ATHENA:
		case DSC_ELEKTRA:
		case DSC_DMM16AT:
		case DSC_DMMAT:
		case DSC_HERCEBX:
			rc = request_irq(kpdata->int_level, &at_adint, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			break;

		case DSC_DMM:
			if ( kpdata->scan_enab )
				rc = request_irq(kpdata->int_level, &nonat_adint_scan, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			else
				rc = request_irq(kpdata->int_level, &nonat_adint_sample, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);

			break;

		case DSC_ZMM:
			if ( kpdata->scan_enab )
				rc = request_irq(kpdata->int_level, &zmm_adint_scan, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			else
				rc = request_irq(kpdata->int_level, &zmm_adint_sample, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);

			break;

		default:
			printk("dscudkp: initialize_adint(): unsupported board type %d\n", kpdata->boardtype);
			rc = -EINVAL;
			break;
	}

	/* request_irq() failed or unknown board */
	if ( rc ) {
		printk("dscudkp: initialize_adint(): request_irq() for irq %d failed with rc=%d\n", kpdata->int_level, rc);
		vfree(kpdata->samples);
		kpdata->samples = NULL;
		kpdata->pCurPos = NULL;
		kpdata->pReadPos = NULL;
		kpdata->irq_active = 0;
	}

	return rc;
}


/*
 * initialize_daint() - Process configuration from ioctl() for a DA interrupt.
 */
static int initialize_daint(KP_DATA *kpdata, DSCUDKP_CONF *conf)
{
	int rc;

	kpdata->boardtype = conf->boardtype;
	kpdata->base_address = conf->base_address;
	kpdata->int_level = conf->int_level;
	kpdata->flipflop_offset = conf->flipflop_offset;
	kpdata->dump_threshold = conf->dump_threshold;
	kpdata->max_transfers = conf->max_transfers;
	kpdata->int_type = conf->int_type;
	kpdata->high_channel = conf->high_channel;
	kpdata->low_channel = conf->low_channel;
	kpdata->scan_enab = conf->scan_enab;
	kpdata->cycle_mode = conf->cycle_mode;
	kpdata->user_int_mode = conf->user_int_mode;

	kpdata->samples_per_int = 1 + kpdata->high_channel - kpdata->low_channel;
	if ( kpdata->max_transfers % kpdata->samples_per_int ) {
		printk("dscudkp: initialize_daint(): invalid max_transfers %ld, must be a multiple of %d\n", kpdata->max_transfers, kpdata->samples_per_int);
		return -EINVAL;
	}

	if ( kpdata->max_transfers < 1 ) {
		printk("dscudkp: initialize_daint(): invalid max_transfers %ld\n", kpdata->max_transfers);
		return -EINVAL;
	}

	kpdata->samples = vmalloc(kpdata->max_transfers * sizeof(SWORD));
	if ( kpdata->samples == NULL ) {
		printk("dscudkp: initialize_daint(): vmalloc of %ld bytes failed\n", kpdata->max_transfers * sizeof(SWORD));
		return -ENOMEM;
	}

	kpdata->pCurPos = kpdata->samples;
	kpdata->cur_transfers = 0;
	kpdata->irq_active = 1;
	kpdata->cur_channel = kpdata->low_channel;
	rc = -EINVAL;

	switch (kpdata->boardtype) {
		case DSC_DMM48:
		case DSC_DMM32:
		case DSC_DMM32XAT:
		case DSC_PROM:
		case DSC_ATHENA:
		case DSC_ELEKTRA:
		case DSC_DMM16AT:
		case DSC_DMMAT:
		case DSC_HERCEBX:
			rc = request_irq(kpdata->int_level, &generic_daint, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			break;

		case DSC_DMM:
			rc = request_irq(kpdata->int_level, &generic_daint, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			break;

		case DSC_ZMM:
			rc = request_irq(kpdata->int_level, &generic_daint, SA_INTERRUPT|SA_SHIRQ, "DSCUD", kpdata);
			break;

		default:
			printk("dscudkp: initialize_daint(): unsupported board type %d\n", kpdata->boardtype);
			rc = -EINVAL;
			break;
	}

	/* request_irq() failed or unknown board */
	if ( rc ) {
		printk("dscudkp: initialize_daint(): request_irq() for irq %d failed with rc=%d\n", kpdata->int_level, rc);
		vfree(kpdata->samples);
		kpdata->samples = NULL;
		kpdata->pCurPos = NULL;
		kpdata->irq_active = 0;
	}

	return rc;
}


/*
 * dscud_ioctl() - Handle ioctl(/dev/dscud) for configuring the board.  See
 * dscudkp.h for information on the configuration struct and the IOCTL 
 * command number.
 */
static int dscud_ioctl(struct inode *node, struct file *filep, unsigned int cmd, 
					unsigned long arg)
{
	KP_DATA *kpdata = (KP_DATA *)filep->private_data;
	DSCUDKP_CONF conf;
	int rc;

	/* ioctl(dev_dscud_fd, IOCTL_DSCUDKP_CONF, &kpconf) */
	if ( cmd == IOCTL_DSCUDKP_CONF ) {

		/* User only allowed to call ioctl successfully once.  If they 
		 * want to reconfigure they most close() and reopen. */
		if ( kpdata->irq_active ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_CONF attempt to configure board twice\n");
			return -EBUSY;
		}

		/* Copy the *DSCUDKP_CONF from user land to kernel land */
		rc = copy_from_user((void *)&conf, (void *)arg, sizeof(DSCUDKP_CONF));
		if ( rc ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_CONF copy_from_user() failed with rc=%d\n", rc);
			return -EFAULT;
		}


		switch (conf.int_type) {
			case INT_TYPE_AD:
				return initialize_adint(kpdata, &conf);

			case INT_TYPE_DA:
				return initialize_daint(kpdata, &conf);

			case INT_TYPE_COUNTER:
			case INT_TYPE_DIOIN:
			case INT_TYPE_OPTO:
			case INT_TYPE_USER:
				return initialize_userint(kpdata, &conf);

			default:
				return -EINVAL;
		}

	/* ioctl(dev_dscud_fd, IOCTL_DSCUDKP_DATA, &data) */
	/* For D/A Interrupt support  only */
	} else if ( cmd == IOCTL_DSCUDKP_DATA ) {
		if ( !kpdata->irq_active ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_DATA attempt to setup DA interrupt buffer with no operation in progress\n");
			return -EINVAL;
		}

		if ( kpdata->samples == NULL ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_DATA invalid DA interrupt buffer sent to kernel\n");
			return -EINVAL;
		}

		rc = copy_from_user((void *)kpdata->samples, (void *)arg, kpdata->max_transfers * sizeof(SWORD));
		if ( rc ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_DATA copy_from_user() failed with rc=%d\n", rc);
			return -EFAULT;
		}

	} else if ( cmd == IOCTL_DSCUDKP_GET_TRANSFERS ) {

		/* Copy the current transfers to userland */
		rc = copy_to_user((void *)arg, (void *)&kpdata->cur_transfers, sizeof(DWORD));
		if ( rc ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_GET_TRANSFERS copy_to_user() failed with rc=%d\n", rc);
			return -EFAULT;
		}

	} else if ( cmd == IOCTL_DSCUDKP_GET_OPTOSTATE ) {

		/* Copy the last opto state to userland */
		rc = copy_to_user((void *)arg, (void *)&kpdata->dmm48_opto_state, sizeof(BYTE));
		if ( rc ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_GET_OPTOSTATE copy_to_user() failed with rc=%d\n", rc);
			return -EFAULT;
		}

	} else if ( cmd == IOCTL_DSCUDKP_GET_VERSION ) {
		WORD version = DSC_VERSION;

		/* Copy DSC_VERSION to userland */
		rc = copy_to_user((void *)arg, (void *)&version, sizeof(WORD));
		if ( rc ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_GET_VERSION copy_to_user() failed with rc=%d\n", rc);
			return -EFAULT;
		}

	} else if ( cmd == IOCTL_DSCUDKP_FREE_IRQ ) {
		if ( kpdata->irq_active ) {
			free_irq(kpdata->int_level, kpdata);
			kpdata->irq_active = 0;
		}

	} else if ( cmd == IOCTL_DSCUDKP_PCI_LIST ) {
		rc = copy_to_user((void *)arg, (void *)&dsc_pci_boards, 
			sizeof(DSCUDKP_PCI_INFO)*DSC_PCI_MAX_BOARDS);

		if ( rc ) {
			printk("dscudkp: dscud_ioctl(): IOCTL_DSCUDKP_PCI_LIST copy_to_user() failed with rc=%d\n", rc);
			return -EFAULT;
		}

	/* ioctl(dev_dscud_fd, UNKNOWN_COMMAND) ? */
	} else {
		printk("dscudkp: dscud_ioctl(): got unknown ioctl code %d\n", cmd );
		return -ENOTTY;
	}	

	return 0;		
}



/*
 * The following wrappers are meant to make things work with 2.0 kernels
 */
#ifdef LINUX_20
int dscud_read_20(struct inode *ino, struct file *f, char *buf, int count)
{
		 return (int)dscud_read(f, buf, count, &f->f_pos);
}

void dscud_release_20(struct inode *ino, struct file *f)
{
		 dscud_release(ino, f);
}

/* Redefine "real" names to the 2.0 ones */
#define dscud_read dscud_read_20
#define dscud_release dscud_release_20
#endif /* LINUX_20 */

/* 
 * Initialize file_operations structure used to attach to events on /dev/dscud 
 * such as open/close.
 */
static struct file_operations dscud_fops = {
	open:  dscud_open,
	release:  dscud_release,
	ioctl:  dscud_ioctl,
	read:  dscud_read,
};


/*
 * init_module() - Initialize the module and load time
 */
int init_module(void)
{
	int rc, b, i, r;
	struct pci_dev *dev = NULL;

#ifndef LINUX_26
	EXPORT_NO_SYMBOLS;
#endif

	SET_MODULE_OWNER(&dscud_fops);

	rc = register_chrdev(dscud_major, "dscud", &dscud_fops);
	if ( rc < 0 )
		return rc;

	memset(dsc_pci_boards, 0, sizeof(DSCUDKP_PCI_INFO)*DSC_PCI_MAX_BOARDS);

	for ( b = 0, i = 0; DSC_PCI_DEVICES[i].device_id != 0 && 
							b < DSC_PCI_MAX_BOARDS; i++ ) 
	{
		do {
			dev = pci_find_device(DSC_PCI_VENDOR_ID, 
				DSC_PCI_DEVICES[i].device_id, dev);

			if ( dev != NULL ) {

				for ( r = 0; r < DEVICE_COUNT_RESOURCE; r++ ) {
					if ( dev->resource[r].flags & IORESOURCE_IO ) {
						dsc_pci_boards[b].port = dev->resource[r].start;
						break;
					}
				}

				if ( r == DEVICE_COUNT_RESOURCE ) {
					printk("dscudkp: unable to lookup I/O port for device\n");
					continue;
				}

				dsc_pci_boards[b].irq = dev->irq;
				dsc_pci_boards[b].slot = PCI_SLOT(dev->devfn);

				dsc_pci_boards[b].device_id = DSC_PCI_DEVICES[i].device_id;
				dsc_pci_boards[b].boardtype = DSC_PCI_DEVICES[i].boardtype;
				b++;
			}

		} while ( dev != NULL && b < DSC_PCI_MAX_BOARDS );
	}

	return 0;
}


