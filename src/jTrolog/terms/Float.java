package jTrolog.terms;

/**
 * Float class represents the float prolog data type
 */
public class Float extends Number {
	
	private float value;

    public Float() {
    }

    public Float(float v) {
		value=v;
	}
	
    public Float(String v) {
		value= java.lang.Float.parseFloat(v);
	}

	/**
	 *  Returns the value of the Float as int
	 */
	public int intValue() {
		return (int) value;
	}
	
	/**
	 *  Returns the value of the Float as float
	 */
	public float floatValue() {
		return value;
	}
	
	/**
	 *  Returns the value of the Float as double
	 */
	public double doubleValue() {
		return value;
	}
	
	/**
	 *  Returns the value of the Float as long
	 */
	public long longValue() {
		return (long) value;
	}
	
	public String toString() {
		return java.lang.Float.toString(value);
	}

    public boolean equals(Object n) {
        if (n instanceof Float)
            return Number.compareDoubleValues(this, (Number) n) == 0;
        return false;
    }
}