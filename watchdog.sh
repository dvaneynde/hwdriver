#!/bin/bash

# http://stackoverflow.com/questions/696839/how-do-i-write-a-bash-script-to-restart-a-process-if-it-dies

until myserver; do
    echo "Server 'myserver' crashed with exit code $?.  Respawning.." >&2
    sleep 1
done

# ------------

sleep 1 &
PID1=$!
sleep 2 &
PID2=$!

wait $PID1
echo 'PID1 has ended.'
wait
echo 'All background processes have exited.'


# -------------

launch backgroundprocess &
PROC_ID=$!

while kill -0 "$PROC_ID" >/dev/null 2>&1; do
    echo "PROCESS IS RUNNING"
done
echo "PROCESS TERMINATED"
exit 0


# ---------

start driver en log
start domotica en log
check driver en domotica; indien domotica 0 stop anders terug