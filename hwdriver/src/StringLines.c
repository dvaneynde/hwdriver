#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "StringLines.h"

int test_StringLines() {
	int i, j, lines;
	char string[2048];
	char **parsed = NULL;

	//strcpy(string, "Hello\nMy friends\nLets parse this string!Hello\nMy friends\nLets parse this string!Hello\nMy friends\nLets parse this string!\nKiekeboe");
	//strcpy(string,"1\n2\n3\n4\n5\n6\n7");	//OK
	//strcpy(string,"1\n2\n3\n4\n5\n6\n");	//OK
	//strcpy(string,"1\n2\n3\n4\n5\n\n");	//OK
	//strcpy(string,"1\n2\n3\n4\n5\n6\n7\n");	//NOK

	strcpy(string, "INIT 1000000\nBOARD_INIT O 0x380 D\nBOARD_INIT D 0x320 D A 0 1\nSET_DO 0x380 0\nSET_DO 0x320 0\nSET_AO 0x320 0 0 1 0\n\n");

	//
	printf("Number of lines:%d\n\n",getLineCount(string));
	fflush(NULL);

	// Parse string
	lines = parseStringbyLines(string, &parsed);
	if (!lines) {
		printf("Parsing failed.\n");
		return 0;
	}

	// Print parsed string
	printf("%d lines parsed.\n\n", lines);
	for (i = 0; i < lines; i++)
		printf("%s\n", parsed[i]);
	fflush(NULL);

	// Free memory
	for (j = 0; j < lines; j++)
		free(parsed[j]);
	free(parsed);

	return 0;
}

int getLineCount(char *buffer) {
	int z = 1;
	char *pch;

	// Find first match
	pch = strchr(buffer, '\n');

	// Increment line count
	while (pch) {
		pch = strchr(pch+1, '\n');
		z++;
	}

	return z;
}

int parseStringbyLines(char *buffer, char ***string) {
	int		*newLine;
	int		b, j, l, z = 1;
	int		lineCount, len;
	char 		*pch, **temp = NULL;

	/*
	** Get line count
	** Allocate memory for new line handling
	** Check if memory allocating failed
	*/
	lineCount = getLineCount(buffer);
	// Original, but buggy. See test lines higher. newLine = (int *)malloc(lineCount + sizeof(int) * sizeof(*newLine));
	newLine = (int *)malloc((lineCount + 1) * sizeof(int));
	if (!newLine)
		return 0;
	newLine[0] = 0;

	// Find first occurance of a new line
	pch = strchr(buffer, '\n');
	if (!pch)
		return 0;

	// If found, find all positions (lijkt op de positie na de newline, 0-based, of de positie van de newline 1-based)
	while (pch) {
		newLine[z] = pch-buffer+1;
		pch = strchr(pch+1, '\n');
		z++;
	}
	newLine[z] = (int)strlen(buffer) + 1;	// (positie na string, 1-based)

	// Allocate memory to our temporary pointer
	temp = (char **)malloc(lineCount * (sizeof *temp));
	if (!temp)
		return 0;

	// Go through all lines found
	for (l = 0; l < z; l++) {
		b = 0;
		len = ((newLine[l+1]-1) + (newLine[l]) + 1);

		// Allocate memory per index
		temp[l] = (char *)malloc(len * sizeof(**temp));
		if (!temp[l])
			return 0;

		// Put our data in
		for (j = newLine[l]; j < newLine[l+1]-1; j++) {
			temp[l][b] = buffer[j];
			b++;
		}
		temp[l][b] = '\0';
	}

	// Free memory for line position
	free(newLine);

	// Set our pointer to point to char **temp
	*string = temp;

	// Return lines found
	return z;
}
