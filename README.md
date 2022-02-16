# Home Automation Hardware Driver

> Note: git log shows too many entries, repository was once an everything-combined repo, which was not a good idea.

C program that communicates with hardware - Diamond Systems - on one hand, and via TCP/IP with the other side - the Java program.

Must run as root.

On MacOS compile `dscud_mock.c` which mocks the hardware. It does not actually do anything, just for debugging rest of code. 

For the Makefile used on a real system see [Deployment Makefile](../home-automation/deployment/makefile/Makefile).