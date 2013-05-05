#!/bin/bash

# Script, in domotic.tar, dat op domotica bordje moet uitgevoerd worden. Vooral compilatie van Driver.
# 10. creeer nieuw directory, met timestamp
# 20. 
# 30. copieer jar en C files
# 40. build HwDriver
# 50. herleg links

# Fallback
# 10. herleg links
# 20. herstart domotica

# Debug
# set -x

# Stop on error
set -e

if [ "$1" = "" ]
then
  echo "Usage: $0 <domotic hostname>"
  exit
fi
DOMHOST=$1

DOMDIR=/home/dirk/domotic
NEWDIR=$(date +%F_%T)
NEWPATH=$DOMDIR/$NEWDIR
export DOMDIR
export NEWDIR
export NEWPATH

#10
echo Nieuwe directory aanmaken op domotica systeem, $NEWDIR
ssh dirk@$DOMHOST <<END
cd $DOMDIR
mkdir $NEWDIR
END
if [[ $? -ne 0 ]]
then
echo "FOUT - Kan domotic directory op $DOMHOST niet aanmaken."
exit 1
fi

#30
echo Kopieren van domotic.tar naar domotica systeem, en uitpakken in $NEWDIR
scp domotic.tar dirk@$DOMHOST:$NEWPATH
ssh dirk@$DOMHOST <<END
cd $NEWPATH
tar -x -f domotic.tar
END

#40
echo En nu het hardware aanstuurprogramma bouwen, dit duurt even...
ssh dirk@$DOMHOST <<END
cd $NEWPATH
make clean all
END

#50
echo Tenslotte, de verbindingen herleggen van 'domotic.jar' en 'hwdriver'
ssh dirk@$DOMHOST <<END
cd $DOMDIR
rm -f domotic.jar
rm -f hwdriver
ln -s $NEWDIR/domotic*.jar domotic.jar
ln -s $NEWDIR/hwdriver hwdriver
