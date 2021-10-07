#! /bin/bash

# Debug
# set -x

echo clean up work directory and .tar file
rm -f ./domotic.tar work/*
rmdir work
mkdir work

echo copying files into work...
cp ../../hwdriver/src/*.[ch] ./work/
rm -f work/target.h
rm -f work/*mock*
cp template/* work/
cp ../../domotic/DomoticConfig* work/
cp ../../domotic/DiamondBoardsConfig* work/
cp ../../domotic/target/domotic*dependencies.jar work/

echo tar into ./domotic.tar
cd work
tar -c -f ../domotic.tar *
cd ..
