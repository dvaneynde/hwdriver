/**
 * TODO This package must only contain DiamondSys specifics. Need to introduce IPhysicalChannel and FysCh implements it.
 * 
 * Abstraction of the Hardware - being a CPU board and a number of I/O cards - through which users can set digital or analog output, and get digital or analog inputs.
 * <p>Users of this Hardware abstraction should only know about a {@link IHardwareIO} implementation, realize themselves {@link IScanEventListener} and use {@link String}.
 * <p>Class {@link OpalmmBoard} shows what I/O boards are capable of only to be used internally, not for users.
 */
package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.IHardwareIO;
