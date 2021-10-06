#!/bin/bash

DOMDIR=/home/dirk/domotic
PIDFILE=$DOMDIR/domotic.pid
INITSCRIPT=/etc/init.d/domotic.sh

checkprocess() {
	if [[ -f $1 ]]; then
		pid=$(cat $1)
		ps -p $pid >/dev/null
		if [ $? -ne 0 ]
		then
			echo "Found pid file $1 but not its process ${pid}. Will restart domotic after 10 sec sleep."
			sleep 10
			echo "Restart..."
			sudo $INITSCRIPT restart
		fi
	fi
}

checkprocess $PIDFILE
