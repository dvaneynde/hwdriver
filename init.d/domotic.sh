#! /bin/bash
#
# Start domotic system at startup.
#
# Author:	Dirk Vaneynde
#
# Version:	0.1
#

#set -x

DOMDIR=/home/dirk/domotic
BOOTLOG=$DOMDIR/domotic.out
echo Boot Domotic | tee -a $BOOTLOG
date | tee -a $BOOTLOG
id | tee -a $BOOTLOG

PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
NAME=domotic
DESC="domotica v2 system"
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
	/usr/bin/java -jar $DOMDIR/domotic.jar domo -l $DOMDIR/log4j.properties -b $DOMDIR/DomoticConfig.xml -c $DOMDIR/DiamondBoardsConfig.xml -d $DOMDIR/hwdriver >>$BOOTLOG 2>&1 &
	echo "Domotic started." | tee -a $BOOTLOG
}

#
#	Function that stops the daemon/service.
#
d_stop() {
	cd $DOMDIR
	if [ ! \( -f $PIDFILE \) ]
	then
		echo "No $PIDFILE file found, cannot stop domotic system." | tee -a $BOOTLOG
		exit 0
	fi
	kill $(cat $PIDFILE) 
	ps -p $(cat $PIDFILE) >/dev/null
	if [ $? -ne 0 ]
	then
		echo " can't stop it, doing it the hard way." | tee -a $BOOTLOG
		kill -9 $(cat $PIDFILE) >/dev/null
		kill -9 $(cat $PIDFILE_DRIVER) >/dev/null
	fi
	ps -p $(cat $PIDFILE) >/dev/null
	if [ $? -eq 0 ]
	then echo "Domotic system stopped." && rm -f "$PIDFILE" | tee -a $BOOTLOG
	fi
	ps -p $(cat $PIDFILE_DRIVER) >/dev/null
	if [ $? -eq 0 ]
	then echo "Driver for Domotic system stopped." && rm -f "$PIDFILE_DRIVER" | tee -a $BOOTLOG
	fi
	echo "Done." | tee -a $BOOTLOG
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
