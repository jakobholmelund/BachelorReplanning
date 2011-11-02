package jTrolog.terms;

/**
 * Long class represents the long prolog data type
 */
public class Long extends Int {

	private long value;

	public Long(long v) {
		value = v;
	}

	public Long(String val) {
		value = java.lang.Long.parseLong(val);
	}

	/**
	 *  Returns the value of the Integer as int
	 */
	final public int intValue() {
		if (value > Integer.MAX_VALUE)
			throw new RuntimeException("value not intable");
		return (int) value;
	}

	/**
	 *  Returns the value of the Integer as float
	 */
	final public float floatValue() {
		return (float) value;
	}

	/**
	 *  Returns the value of the Integer as double
	 */
	final public double doubleValue() {
		return (double) value;
	}

	/**
	 *  Returns the value of the Integer as long
	 */
	final public long longValue() {
		return value;
	}

	public String toString() {
		return java.lang.Long.toString(value);
	}
}