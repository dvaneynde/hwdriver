#!/bin/bash
#
# Start domotic system as a service.
# For version 3.0 (with logback, java 8)
# 
# Author:	Dirk Vaneynde
# Version:	4/3/2017
#

#set -x

DOMDIR=/home/dirk/domotic
BOOTLOG=$DOMDIR/boot.out
echo Boot Domotic | tee -a $BOOTLOG
date | tee -a $BOOTLOG
id | tee -a $BOOTLOG

PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
NAME=domotic
DESC="domotica v3 system"
PIDFILE=$DOMDIR/domotic.pid
PIDFILE_DRIVER=$DOMDIR/driver.pid
SCRIPTNAME=/etc/init.d/$NAME

# Gracefully exit if the package has been removed.
# test -x $DAEMON || exit 0

#
#	Function that starts the daemon/service.
#
d_start() {
	if [[ -f $PIDFILE ]]; then
		ps -p $(cat $PIDFILE) >/dev/null 2>&1
		if [ $? -eq 0 ]
		then 
			echo "Domotic system already running." | tee -a $BOOTLOG
			exit 0
		fi
	fi
	cd $DOMDIR
	/usr/bin/java -Dlogback.configurationFile=$DOMDIR/logback.xml -jar $DOMDIR/domotic.jar domo -t 20 -b $DOMDIR/DomoticConfig.xml -c $DOMDIR/DiamondBoardsConfig.xml -d $DOMDIR/hwdriver -w $DOMDIR/static >>$BOOTLOG 2>&1 &
	echo "Domotic started." | tee -a $BOOTLOG
}

# kill process of which id is in file given by 1st parameter
killprocess() {
	if [ ! \( -f $1 \) ]
	then
	echo "No pidfile $1 found, cannot stop domotic system." | tee -a $BOOTLOG
	else
		pid=$(cat $1)
		kill $pid
		sleep 1
		ps -p $pid 
		if [ $? -eq 0 ]
		then
			echo "Cannot stop process ${pid}, doing it the hard way." | tee -a $BOOTLOG
			kill -9 $pid >/dev/null 2>&1 
		fi
		rm -f $1
		echo "Stopped process ${pid}, removed file ${1}." | tee -a $BOOTLOG
	fi		
}

#
#	Function that stops the daemon/service.
#
d_stop() {
	cd $DOMDIR
	echo "Stopping domotic system (java program)..."
	killprocess $PIDFILE
	echo "Stopping hardware driver..."
	killprocess $PIDFILE_DRIVER
}

#
#	Function that sends a SIGHUP to the daemon/service.
#
d_reload() {
	WARNING reloading $NAME not implemented yet | tee -a $BOOTLOG
}

case "$1" in
  start)
	echo "Starting $DESC: $NAME" | tee -a $BOOTLOG
	d_start
	;;
  stop)
	echo "Stopping $DESC: $NAME" | tee -a $BOOTLOG
	d_stop
	;;
  restart|force-reload)
	echo "Restarting $DESC: $NAME" | tee -a $BOOTLOG
	echo "First stop..." | tee -a $BOOTLOG
	d_stop
	sleep 1
	echo "Then start up..." | tee -a $BOOTLOG
	d_start
	;;
  *)
	# echo "Usage: $SCRIPTNAME {start|stop|restart|reload|force-reload}" >&2
	echo "Usage: $SCRIPTNAME {start|stop|restart}" >&2
	exit 1
	;;
esac

exit 0
