#! /bin/bash
#
# Start domotic system at startup.
#
# Author:	Dirk Vaneynde
#
# Version:	0.1
#

#set -x

DOMDIR=/home/dirk/domotica
BOOTLOG=$DOMDIR/boot.log
echo Boot Domotica >$BOOTLOG
date >>$BOOTLOG
id >>$BOOTLOG 2>&1

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
	ps -p $(cat $PIDFILE) >/dev/null
	if [ $? -eq 0 ]
	then 
		echo "Domotic system already started."
		exit 0
	fi
	cd $DOMDIR
	nohup /opt/jdk/bin/java -jar domotica.jar -l log4j.properties -c ./domotic-cfg.xml -d ./hwdriver >/dev/null 2>&1 &
	echo "Domotic started."
}

#
#	Function that stops the daemon/service.
#
d_stop() {
	cd $DOMDIR
	if [ ! \( -f $PIDFILE \) ]
	then
		echo
		echo "No $PIDFILE file found, cannot stop domotic system."
		exit 0
	fi
	kill $(cat $PIDFILE) 
	ps -p $(cat $PIDFILE) >/dev/null
	if [ $? -ne 0 ]
	then
		echo " can't stop it, doing it the hard way."
		kill -9 $(cat $PIDFILE) >/dev/null
		kill -9 $(cat $PIDFILE_DRIVER) >/dev/null
	fi
	ps -p $(cat $PIDFILE) >/dev/null
	if [ $? -eq 0 ]
	then echo "Domotic system stopped." && rm -f "$PIDFILE"
	fi
	ps -p $(cat $PIDFILE_DRIVER) >/dev/null
	if [ $? -eq 0 ]
	then echo "Driver for Domotic system stopped." && rm -f "$PIDFILE_DRIVER"
	fi
	echo "Done."
}

#
#	Function that sends a SIGHUP to the daemon/service.
#
d_reload() {
	WARNING reloading $NAME not implemented yet
}

case "$1" in
  start)
	echo "Starting $DESC: $NAME"
	d_start
	echo "."
	;;
  stop)
	echo "Stopping $DESC: $NAME"
	d_stop
	echo "."
	;;
  restart|force-reload)
	echo "Restarting $DESC: $NAME"
	echo "First stop..."
	d_stop
	sleep 1
	echo "Then start up..."
	d_start
	echo "."
	;;
  *)
	# echo "Usage: $SCRIPTNAME {start|stop|restart|reload|force-reload}" >&2
	echo "Usage: $SCRIPTNAME {start|stop|restart}" >&2
	exit 1
	;;
esac

exit 0
