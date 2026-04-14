# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**hwdriver** is a C program that bridges Diamond Systems I/O hardware boards (DMMAT and Opalmm) to a Java home automation server over TCP/IP. It runs as a TCP server on port 4444, accepting text-based commands to read sensors and control actuators. Must run as root on Linux (requires hardware port access via `ioperm()`).

## Build Commands

```bash
make          # Build the hwdriver executable
make clean    # Remove build artifacts
```

Run the server:
```bash
./hwdriver [-d] <hostname>
# -d: enable debug logging
# hostname: IP to bind to, e.g. 0.0.0.0
```

## Platform / Compilation

The file [src/target.h](src/target.h) controls platform selection via `#define OSX`:
- **macOS (OSX defined):** Links `src/dscud_mock.c` (stub Diamond SDK) and uses no-op Opalmm stubs. For development/debugging only — real hardware does not work.
- **Linux (OSX not defined):** Links `dscud5/libdscud5.a` (real Diamond SDK) and uses actual `ioperm()`-based I/O.

The production Makefile lives outside this repo at `../home-automation/deployment/makefile/Makefile`.

## Architecture

```
HwDriver.c  — TCP server, command dispatcher, main loop
  ├── Dmmat.c/h     — DMMAT board: digital + analog I/O via Diamond SDK
  ├── Opalmm.c/h    — Opalmm board: digital I/O via direct port access (ioperm)
  ├── dscud_mock.c  — macOS stubs for the Diamond SDK (dscud5/)
  ├── log.c/h       — Levelled logging (TRACE/DEBUG/INFO/WARN/ERROR/FATAL)
  └── StringLines.c/h — Line-oriented message parser
```

### Protocol

Text-based, newline-delimited. Commands received over TCP socket, response sent back:

| Command | Parameters | Description |
|---------|-----------|-------------|
| `INIT` | — | Initialize Diamond driver (`dscInit`) |
| `BOARD_INIT` | `<addr> <type>` | Register board (`O`=Opalmm, `D`=DMMAT) |
| `REQ_INP` | `<addr>` | Read inputs from board at address |
| `SET_OUT` | `<addr> <type> - <vals>` | Write outputs to board |
| `PING` | — | Echo test |
| `QUIT` | — | Shutdown server |

### Board Registry

`address2dscbs[10]` in `HwDriver.c` maps hardware I/O port addresses to Diamond SDK board handles (`DSCB`). DMMAT boards use the Diamond SDK; Opalmm boards use direct port I/O.

### PID File

On startup, the process writes its PID to `driver.pid` in the current working directory.

## Testing

There is no automated test suite. Manual testing:
1. Build and start: `./hwdriver -d 0.0.0.0`
2. Connect via `nc localhost 4444` and send protocol commands
3. `StringLines.c` contains `test_StringLines()` (not wired to a test runner — call manually to verify parsing)
