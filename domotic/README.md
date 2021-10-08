# domotica

## How to run

```bash
# For help
$ java -jar domotica-1.0-jar-with-dependencies.jar -h

Usage:	Main domo [-s] [-r] [-d path2Driver] [-t looptime] [-h hostname] [-p port] [-w webapproot] -b blocks-config-file -c hardware-config-file
	Main hw [-d path2Driver] [-h hostname] [-p port] -c hardware-config-file
	-s simulate hardware driver (domotic only, for testing and development)
	-d path to driver, if it needs to be started and managed by this program
	-t time between loops, in ms; defaults to 20 ms.
	-h hostname of hardware driver; incompatible with -d
	-p port of hardware driver; incompatible with -d	
    -w path of directory with webapp (where index.html is located)
	-b domotic blocks xml configuration file
	-c hardware xml configuration file
To configure logging externally, use 'java -Dlogback.configurationFile=/path/to/config.xml ...' or system env variable.
Domotica Main


# To run in simulation mode
$ java -jar domotica-1.0-jar-with-dependencies.jar -Dlogback.configurationFile=src/main/resources/logback-dev.xml \
    domo -s -c DiamondBoardsConfig.xml -b DomoticConfig.xml -w static
```

## TODO

- HardwareIO heeft al exit 1 !!!
- xml resources in src/main/resources, as default
- logging config in src/main/resources, as default
- logging.properties ook op bordje, en -D bij domotic.sh

