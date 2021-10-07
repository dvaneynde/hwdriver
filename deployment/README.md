# Deployment

## Build & deploy

On development machine:

```bash
cd install
./install.sh domotica # or any hostname or ip
```

Then on server:
```bash
ssh domotica
cd domotic
# check that software was correctly installed, symbolic links correct
sudo /etc/init.d/domotic.sh restart
```

For the UI, see [UI README](../elm-ui/README.md).

## Set up Services & Health

See `scripts` folder, these scripts must be installed on the Ubuntu server.

- `domotic.sh` : copy into /etc/init.d/domotic.sh
- `watchdog.sh` : copy into ~/domotic


`watchdog.sh` will check domotic.pid, and if process is not there but domotic.pid is, it will restart via 'domotic.sh restart'. For this to work you need to define a cron job.

```bash
$ sudo crontab -l
# m h  dom mon dow   command
# Every minute run watchdog for domotic
* * * * *	/home/dirk/domotic/watchdog.sh >/home/dirk/domotic/cron.out
```