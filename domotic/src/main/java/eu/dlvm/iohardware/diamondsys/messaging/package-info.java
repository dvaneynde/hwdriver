/**
 * TODO review
 * Everything to communicate with a Hardware Driver (HW) program, written in a language that can communicate directly with real hardware, typically C or Basic
 * programs.
 * <p>
 * Communication with the HW is a request-reply-sleep loop, typically in two phases. <br/>
 * 
 * <pre>
 * Domotic		HW
 * 		--> request for input values
 * 		<-- returns input values
 * 		--> set output values
 * sleep
 * back to first one
 * </pre>
 * <p>
 * At startup however there is first an initialisation request. And the Domotic can always send a STOP request, the HW an ERROR message.
 * <p>
 * <p>
 * In good unix/internet tradition, these messages are readable newline-separated text text strings sent over tcp/ip. There is one message per line. To indicate
 * that it is the other one's turn, an empty line is sent.
 * <p>
 * As a naming convention, message classes that start with 'Msg2' are sent to the Hardware Driver, the others are received.
 * <p>
 * The HW program starts and then the following typically happens, where THIS is the {@link IHwIOprocessor} implementation (or the Java program communicating
 * with the Hardware Driver):
 * <ol>
 * <li>THIS-->HW {@link Msg2InitHardware} and multiple {@link Msg2InitBoard} to initialize the Hardware Driver and any boards.</li>
 * <li>THIS-->HW REQ_VAL_DI, REQ_VAL_AI</li>
 * <li>HW --> THIS with any combination of 1 or more {@link MsgValDi}, {@link MsgValAi}, {@link MsgError}</li>
 * <li>HW <-- THIS with instructions to set output {@link Msg2OutputAnalog} or {@link Msg2OutputDigital}.</li>
 * </ol>
 * <p>
 * Note that runtime re-configuration of the boards is not possible. Would be difficult too, you need to replace the physical boards as well while the system is
 * running - not sure it will survive that.
 */
package eu.dlvm.iohardware.diamondsys.messaging;


