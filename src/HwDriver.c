/*
 ============================================================================
 Name        : HwDriver.c
 Author      : Dirk Vaneynde
 Version     : 0.1
 Copyright   : DLV Mechanografie
 Description : Domotica Hardware Adaptor
 ============================================================================

 TODO hoe zit het met buffers, 2 lege lijnen, detectie request gedaan en reply gedaan...
 TODO init voor DIG-IO, om eenmalig r/w toegang te krijgen tot poorten
 */

#include <sys/errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <time.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#include "dscud.h"
#include "HwDriver.h"
#include "Opalmm.h"
#include "Dmmat.h"
#include "log.h"
#include "StringLines.h"

#define LENGTH(x) (sizeof(x)/sizeof(*(x)))

#define PORT	4444		// port of Domotica server
#define MSGSIZE	2048

int debug;
long msgCounter = 0;

char hostname[256];
char msgIn[MSGSIZE]; // Buffer for incoming message
char msgOut[MSGSIZE]; // Buffer for outgoing message
struct sockaddr_in pin;
struct hostent *hp;
int servSock;
int clntSock;
struct sockaddr_in servAddr, clntAddr;

int keepOnGoing = 1;

char logmsg[1024];
char* errordetail;
char errormsg[1024];

#define INIT	0
#define BOARD_INIT	1
#define REQ_INP	2
#define SET_OUT	4
#define QUIT	6

void setupAsServer() {

	if ((hp = gethostbyname(hostname)) == 0) {
		errordetail = strerror(errno);
		strcat(strcpy(errormsg, "gethostbyname failed. Detail: "), errordetail);
		perror("gethostbyname");
		exit(1);
	}

	/* Create socket for incoming connections */
	if ((servSock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0)
		perror("socket() failed");
	bzero((char *) &servAddr, sizeof(servAddr));
	servAddr.sin_family = AF_INET; /* Internet address family */
	servAddr.sin_addr.s_addr = ((struct in_addr *) (hp->h_addr))->s_addr;
	servAddr.sin_port = htons(PORT);
	if (bind(servSock, (struct sockaddr *) &servAddr, sizeof(servAddr)) < 0)
		perror("bind() failed");
	/* Mark the socket so it will listen for incoming connections */
	if (listen(servSock, 5) < 0)
		perror("listen() failed");
	sprintf(logmsg, "Listening for commands, port=%d\n", PORT);
	mylog(MYLOG_INFO, logmsg);
}

/*
 * Initialiseer Diamond I/O bord software.
 */
void initDiamondDriver() {
	ERRPARAMS errorParams; // structure for returning error code and error string
	if (dscInit(DSC_VERSION) != DE_NONE) {
		dscGetLastError(&errorParams);
		sprintf(logmsg, "dscInit error: %s %s",
				dscGetErrorString(errorParams.ErrCode), errorParams.errstring);
		mylog(MYLOG_FATAL, logmsg);
	}
	mylog(MYLOG_INFO, "Diamond Driver initialized.");
}

/*
 * Stop Diamond I/O bord software.
 */
void endDiamondDriver() {
	dscFree();
}

/*
 * Stuur bericht, de tekst in 'in', naar Domotica Server.
 */
void sendMsg(char* in) {
	/* send a message to the server PORT on machine HOST */
	sprintf(logmsg, "sendMsg(), '%s'\n", in);
	mylog(MYLOG_DEBUG, logmsg);
	if (send(clntSock, in, strlen(in), 0) == -1) {
		perror("send");
		exit(1);
	}
}

/*
 * Ontvang een bericht van de Domotica Server, typisch meerdere lijnen, afgesloten door een lege lijn.
 */
void recvMsg(char* out, int maxlen) {
	int len = 0;
	if ((len = recv(clntSock, out, maxlen, 0)) == -1) {
		perror("recv");
		exit(1);
	}
	out[len] = '\0';
	sprintf(logmsg, "recvMsg(), maxlen=%d, length=%d, text='%s'\n", maxlen, len,
			out);
	mylog(MYLOG_DEBUG, logmsg);
}

/*
 * Gegeven een commandolijn, bepaal het soort commando.
 */
int stringToCommand(char* sCmd) {
	char f = sCmd[0];
	switch (f) {
	case 'I':
		return INIT;
	case 'B':
		return BOARD_INIT;
	case 'R':
		return REQ_INP;
	case 'S':
		return SET_OUT;
	case 'Q':
		return QUIT;
	default:
		mylog(MYLOG_WARN, "Unknown command received.");
		exit(2);
	}
}

/*
 * Neemt 'text' en kapt het in stukken, na uitvoering bevat elke parms[i] een stuk.
 */
int parseRecvdParams(char* text, char** parms) {
	int parmsLen = 0;
	char* parm;
	while ((parm = strtok(text, " ")) != NULL ) {
		text = NULL;
		parms[parmsLen] = malloc(strlen(parm) + 1);
		strcpy(parms[parmsLen], parm);
		parmsLen++;
	}
	return parmsLen;
}

/**
 * 'line' bevat een ontvangen commando: een INIT, BOARD_INIT, SET_DO, SET_AO of QUIT.
 * De overeenkomstige actie wordt uitgevoerd.
 */
void parseRecvdMsgLine(char* line) {
	int i;
	char *sCmd;

	sCmd = strtok(line, " ");
	sprintf(logmsg, "Command: \'%s\'", sCmd);
	mylog(MYLOG_DEBUG, logmsg);
	if ((sCmd == NULL )|| (strlen(sCmd) == 0))return;
	int cmd = stringToCommand(sCmd);

	char* parms = strtok(NULL, "");
	sprintf(logmsg, "parms='%s'", parms);
	mylog(MYLOG_DEBUG, logmsg);

	int address, bVal;
	char* lineParms[128];
	char oneLine[80];
	int nrParms;
	char type;

	switch (cmd) {
	case INIT:
		// Initialisatie I/O bordjes
		initDiamondDriver();
		break;
	case BOARD_INIT:
		/* Parse and add a boardinfo entry
		 * BOARD_INIT O 0x380 D
		 * BOARD_INIT D 0x320 D A 0 1
		 */
		nrParms = parseRecvdParams(parms, lineParms);
		type = lineParms[0][0];
		sscanf(lineParms[1], "0x%x", &(address));
		// Initialize board
		if (type == 'D') {
			dmmatInit(address);
		} else if (type == 'O') {
			opalmmInit(address);
		}
		// Free up
		for (i = 0; i < nrParms; i++)
			free(lineParms[i]);
		break;
	case REQ_INP:
		nrParms = parseRecvdParams(parms, lineParms);
		sscanf(lineParms[0], "0x%x", &address);
		type = lineParms[1][0];
		if (type == 'O') {
			int val = opalmmReadDigIn(address);
			sprintf(msgOut, "%sINP_O 0x%x %d\n", msgOut, address, val);
		} else if (type == 'D') {
			dmmatReadInputs(address, lineParms, oneLine);
			sprintf(msgOut, "%s%s\n", msgOut, oneLine);
		} else {
			sprintf(msgOut, "%sERROR REQ_INPUTS not implemented for type %c\n",
					msgOut, type);
		}
		break;
	case SET_OUT:
		nrParms = parseRecvdParams(parms, lineParms);
		sscanf(lineParms[0], "0x%x", &address);
		type = lineParms[1][0];
		if (type == 'O') {
			sscanf(lineParms[2], "%d", &bVal);
			sprintf(logmsg,
					"SET_OUT command parsed, address(hex)=0x%x, type=%c, val(dec)=%d",
					address, type, bVal);
			mylog(MYLOG_DEBUG, logmsg);
			opalmmSetDigiOut(address, bVal);
		} else {
			// SET_OUT 0x300 D - 1025 2056
			dmmatSetOutputs(address, lineParms);
		}
		break;
	case QUIT:
		mylog(MYLOG_INFO, "\nQUIT command - 't is gedaan!\n");
		close(clntSock);
		endDiamondDriver();
		// TODO close boards properly ? Set stop flag so that run() can stop properly?
		// TODO end opalmm by calling closeOpalmm()
		keepOnGoing = 0;
		break;
	}
}

/*
 * Kapt alle ontvangen commando's in een array van commando's.
 * Een ontvangen lijn is een commando.
 */
void parseRecvdMsg() {
	char **lines = NULL;
	int nrLines, parmsLen, j;

	nrLines = parseStringbyLines(msgIn, &lines);
	if (nrLines==0) {
		mylog(MYLOG_DEBUG, "Empty line received, nothing to do.\n");
		return;
	}

	// Print parsed string
	sprintf(logmsg, "%d lines parsed.\n\n", nrLines);
	mylog(MYLOG_DEBUG, logmsg);
	for (parmsLen = 0; parmsLen < nrLines; parmsLen++)
		parseRecvdMsgLine(lines[parmsLen]);

	// Free memory
	for (j = 0; j < nrLines; j++)
		free(lines[j]);
	free(lines);
}

void runAsServer() {
	// Server socket set up
	setupAsServer();
	// Wait for client to connect
	unsigned int clntLen = sizeof(clntAddr);
	if ((clntSock = accept(servSock, (struct sockaddr *) &clntAddr, &clntLen))
			< 0)
		perror("accept() failed");
	sprintf(logmsg, "Connection established with client, client is on port %d.",
			ntohs(clntAddr.sin_port));
	mylog(MYLOG_INFO, logmsg);
	// Exchange messages until STOP received
	keepOnGoing = 1;
	while (keepOnGoing) {
		// Read and execute received messages
		recvMsg(msgIn, MSGSIZE);
		msgOut[0] = '\0';
		parseRecvdMsg();
		strcat(msgOut, "\n"); // Empty line, to indicate 'end-of reply'
		sprintf(logmsg, "Answering with:\n%s-----", msgOut);
		mylog(MYLOG_DEBUG, logmsg);
		sendMsg(msgOut);
		if ((msgCounter % (20 * 60)) == 0) { // elke minuut
			sprintf(logmsg, "Still alive, msgCounter=%ld.", msgCounter);
			mylog(MYLOG_INFO, logmsg);
		}
		msgCounter++;
	}

}

/*
 * Schrijft het Process ID van dit programma naar het bestand "driver.pid" in huidige working directory.
 */
void writePidToFile() {
	pid_t pid;
	if ((pid = getpid()) < 0) {
		perror("unable to get pid");
	} else {
		printf("The process id is %d", pid);
	}
	FILE *file;
	file = fopen("driver.pid", "w"); // erase contents if exists, and write only
	fprintf(file, "%d", pid);
	fclose(file);
}

/**
 * Hoofdprogramma. Start als "hwdriver -h" om alle opties te weten.
 */
int main(int argc, char *argv[]) {
	int i;
	int debug = 0; /* Value for the "-d" optional argument. */
	if (argc < 2) {
		printf(
				"Usage: %s [-d] hostname\n\t-d: debug\n\thostname: ip or hostname (caller must use same, e.g. 0.0.0.0).\n\n",
				argv[0]);
		exit(1);
	}
	for (i = 1; i < argc; i++) /* Skip argv[0] (program name). */
	{
		if (strcmp(argv[i], "-d") == 0) {
			debug = 1;
		} else {
			strcpy(hostname, argv[i]);
		}
	}

	mylogSetHighestLevel(debug ? MYLOG_DEBUG : MYLOG_INFO);

	writePidToFile();

	mylog(MYLOG_INFO, "HwDriver started.");
	runAsServer();
	mylog(MYLOG_INFO, "HwDriver ended.");

	return EXIT_SUCCESS;
}
