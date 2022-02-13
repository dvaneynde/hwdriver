#
# Makefile for OS X. dscud5 cannot be used here, dummy is used instead. 
#

#LIB=-L../dscud5 -ldscud5 -pthread -lm 
LIB := -pthread -lm -lC
INC := -Idscud5 
BUILD_DIR := ./build
SRC_DIR := ./src
MAIN := hwdriver

all: $(MAIN) 

clean:
	rm -f $(MAIN) $(BUILD_DIR)/*.o

$(MAIN):
	( gcc -O2 -o $(MAIN) $(SRC_DIR)/dscud_mock.c $(SRC_DIR)/log.c $(SRC_DIR)/StringLines.c $(SRC_DIR)/Opalmm.c $(SRC_DIR)/Dmmat.c $(SRC_DIR)/HwDriver.c $(LIB) $(INC) )
#	( gcc -O2 -o $(MAIN) -arch i386 dscud_mock.c log.c StringLines.c Opalmm.c Dmmat.c HwDriver.c $(LIB) $(INC) )

#	( gcc -static -O2 -o $(MAIN) log.c StringLines.c Opalmm.c Dmmat.c HwDriver.c $(LIB) $(INC) )

# werkt op OS X:
# gcc -o hwdriver dscud_mock.c log.c StringLines.c Opalmm.c Dmmat.c HwDriver.c -pthread -lm -lC  -I../dscud5