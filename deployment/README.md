# Deployment

> Setup domotic machine for the first time, including Ubuntu and everything else, see [First-Setup](FIRST-SETUP.md).

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

## Backup
https://ubuntuforums.org/showthread.php?t=35087

sudo su -
cd /
tar cvpzf backup.tgz --exclude=/proc --exclude=/lost+found --exclude=/backup.tgz --exclude=/mnt --exclude=/sys /


