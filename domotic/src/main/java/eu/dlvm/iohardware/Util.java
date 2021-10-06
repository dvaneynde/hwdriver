package eu.dlvm.iohardware;

/**
 * Utilities around bytes and such.
 * @author dirk vaneynde
 */
public class Util {

	/**
	 * Bytes in Java are signed, which is a big problem for controllers etc. 
	 * The only solution is to put it in an int (4 bytes in Java) and only 
	 * work with the LSB.
	 * <p> The &amp; operator already converts its arguments to an int; if it
	 * has the sign bit set (first bit) then 3 first bytes are also '1' (2nd compelemnt).
	 * Then we set the 3 first bytes to 0.
	 * @param b a byte
	 * @return integer with the same value, as if b were an unsigned byte
	 */
	public static int unsign(byte b) {
		return 0xFF & b;
	}
	
	public static String BYTE_HEADER = "7 6 5 4 3 2 1 0";
    public static String WORD_HEADER = "F E D C B A 9 8 7 6 5 4 3 2 1 0";

	/**
	 * Pretty print of a byte.
	 * @param state, for instance 0x03; if an int is passed, only the LSB is taken (I think - not checked)
	 * @return For instance 0 0 0 0 0 0 1 1
	 */
	public static String prettyByte(int state) {
		String s = "";
		int mask = 128;
		do {
			int bit = state & mask;
			s = s + (bit==mask ? 1 : 0) + ' ';
			mask >>>= 1;
		} while (mask > 0);
		return s;
	}
	
}
