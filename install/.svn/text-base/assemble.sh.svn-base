#! /bin/bash

# Debug
# set -x

echo clean up work directory and .tar file
./clean.sh

echo copying files into work...
cp ../../HwDriver/src/*.[ch] ./work/
rm -f work/target.h
rm -f work/*mock*
cp template/* work/
cp ../../domotic/domotic-cfg.xml work/
cp ../../domotic/log4j.properties work/
cp ../../domotic/target/domotic*dependencies.jar work/

echo tar into ./domotic.tar
cd work
tar -c -f ../domotic.tar *
cd ..
