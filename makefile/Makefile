# 
# Makefile for Linux and QNX
#  
# For other operating systems see the build files in these subdirectories.
# Linux and QNX users just type "make" in this directory to build all 
# the demo programs for this board.
#

LIB=-L/usr/local/dscud5 -L/opt/dscud5 -ldscud5 -pthread -lm 
INC=-I/usr/local/dscud5 -I/opt/dscud5

MAIN=hwdriver

TEST=testopalmm

all: $(MAIN) 

clean:
	rm -f $(MAIN) $(TEST) *.o

$(MAIN):
	( gcc -O2 -o $(MAIN) log.c StringLines.c Opalmm.c Dmmat.c HwDriver.c $(LIB) $(INC) )

#	( gcc -static -O2 -o $(MAIN) log.c StringLines.c Opalmm.c Dmmat.c HwDriver.c $(LIB) $(INC) )

$(TEST):
	( gcc -O2 -v -o $(TEST) $(TEST).c $(LIB) $(INC) )

